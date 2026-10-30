package com.mywallet.bank.application.service;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.mywallet.bank.api.BankTokens;
import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.exception.ConcurrentUpdateException;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.model.RejectionReason;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
		"bank.retry.max-attempts=100",
		"spring.datasource.hikari.maximum-pool-size=50"
})
@AutoConfigureMockMvc
class OperationConcurrencyTest extends AbstractApplicationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void sameReferenceFromTwentyThreadsDebitsOnce() throws InterruptedException {
		BankAccount account = persistedAccount(Money.of("1000.00"));
		String reference = "SAME-" + UUID.randomUUID();
		OperationCommand command = new OperationCommand(reference, account.getAccountNumber().value(), "100.00");
		List<Callable<OperationResult>> tasks = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			tasks.add(() -> debitAccountUseCase.execute(command));
		}

		List<AttemptOutcome> outcomes = runTogether(tasks);

		List<OperationResult> results = completed(outcomes);
		assertThat(outcomes).hasSize(20);
		assertThat(results).isNotEmpty().allSatisfy(result -> {
			assertThat(result).isEqualTo(results.getFirst());
			assertThat(result.status()).isEqualTo(OperationStatus.SUCCESS);
			assertThat(result.balanceAfter()).isEqualTo(Money.of("900.00"));
		});
		assertThat(countByReference(reference)).isEqualTo(1);
		assertThat(countByAccount(account.getId())).isEqualTo(1);
		assertThat(balanceOf(account.getId())).isEqualByComparingTo("900.00");
	}

	@Test
	void fiftyParallelDebitsSettleTenSuccessesAndFortyRejections() throws InterruptedException {
		BankAccount account = persistedAccount(Money.of("1000.00"));
		String accountNumber = account.getAccountNumber().value();
		List<Callable<OperationResult>> tasks = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			String reference = "PAR-" + i + "-" + UUID.randomUUID();
			tasks.add(() -> debitThroughApi(accountNumber, reference));
		}

		List<AttemptOutcome> outcomes = runTogether(tasks);
		int successes = count(outcomes, OperationStatus.SUCCESS);
		int rejections = count(outcomes, OperationStatus.REJECTED);

		assertThat(outcomes).allSatisfy(outcome -> assertThat(outcome.error()).isNull());
		assertThat(successes).isEqualTo(10);
		assertThat(rejections).isEqualTo(40);
		assertThat(outcomes).filteredOn(outcome -> outcome.result().status() == OperationStatus.REJECTED)
				.allSatisfy(outcome -> assertThat(outcome.result().rejectionReason())
						.isEqualTo(RejectionReason.INSUFFICIENT_FUNDS));
		assertThat(balanceOf(account.getId())).isEqualByComparingTo("0.00");
		assertThat(countByAccount(account.getId())).isEqualTo(50);
	}

	private OperationResult debitThroughApi(String accountNumber, String reference) throws Exception {
		MvcResult result = mockMvc.perform(post("/bank/operations/debit")
						.with(BankTokens.walletDebit())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"reference":"%s","accountNumber":"%s","amount":"100.00"}
								""".formatted(reference, accountNumber)))
				.andReturn();
		if (result.getResponse().getStatus() != 200) {
			throw new IllegalStateException("Débit refusé par la sécurité : " + result.getResponse().getStatus());
		}
		String body = result.getResponse().getContentAsString();
		String status = JsonPath.read(body, "$.status");
		return new OperationResult(
				reference,
				OperationType.DEBIT,
				OperationStatus.valueOf(status),
				readReason(body),
				readBalance(body),
				Instant.parse(JsonPath.read(body, "$.createdAt"))
		);
	}

	private static RejectionReason readReason(String body) {
		try {
			String reason = JsonPath.read(body, "$.rejectionReason");
			return reason == null ? null : RejectionReason.valueOf(reason);
		} catch (PathNotFoundException exception) {
			return null;
		}
	}

	private static Money readBalance(String body) {
		try {
			String balance = JsonPath.read(body, "$.balanceAfter");
			return balance == null ? null : Money.of(balance);
		} catch (PathNotFoundException exception) {
			return null;
		}
	}

	private static List<OperationResult> completed(List<AttemptOutcome> outcomes) {
		List<OperationResult> results = new ArrayList<>();
		for (AttemptOutcome outcome : outcomes) {
			if (outcome.error() != null) {
				assertThat(outcome.error()).isInstanceOf(ConcurrentUpdateException.class);
				continue;
			}
			results.add(outcome.result());
		}
		return results;
	}

	private static int count(List<AttemptOutcome> outcomes, OperationStatus status) {
		int count = 0;
		for (AttemptOutcome outcome : outcomes) {
			if (outcome.result() != null && outcome.result().status() == status) {
				count++;
			}
		}
		return count;
	}

}
