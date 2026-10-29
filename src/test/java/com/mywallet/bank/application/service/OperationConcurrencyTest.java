package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.exception.ConcurrentUpdateException;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.RejectionReason;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
		"bank.retry.max-attempts=100",
		"spring.datasource.hikari.maximum-pool-size=50"
})
class OperationConcurrencyTest extends AbstractApplicationIntegrationTest {

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
		List<Callable<OperationResult>> tasks = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			OperationCommand command = new OperationCommand(
					"PAR-" + i + "-" + UUID.randomUUID(),
					account.getAccountNumber().value(),
					"100.00"
			);
			tasks.add(() -> debitAccountUseCase.execute(command));
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
