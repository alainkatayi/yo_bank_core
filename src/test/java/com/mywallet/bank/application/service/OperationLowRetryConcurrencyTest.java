package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.exception.ConcurrentUpdateException;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
		"bank.retry.max-attempts=3",
		"spring.datasource.hikari.maximum-pool-size=50"
})
class OperationLowRetryConcurrencyTest extends AbstractApplicationIntegrationTest {

	@Test
	void keepsTheLedgerInvariantsWhenRetriesAreExhausted() throws InterruptedException {
		BankAccount account = persistedAccount(Money.of("1000.00"));
		List<Callable<OperationResult>> tasks = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			OperationCommand command = new OperationCommand(
					"LOW-" + i + "-" + UUID.randomUUID(),
					account.getAccountNumber().value(),
					"100.00"
			);
			tasks.add(() -> debitAccountUseCase.execute(command));
		}

		List<AttemptOutcome> outcomes = runTogether(tasks);
		int successes = 0;
		int rejections = 0;
		int conflicts = 0;
		for (AttemptOutcome outcome : outcomes) {
			if (outcome.error() != null) {
				assertThat(outcome.error()).isInstanceOf(ConcurrentUpdateException.class);
				conflicts++;
				continue;
			}
			if (outcome.result().status() == OperationStatus.SUCCESS) {
				successes++;
			} else {
				assertThat(outcome.result().status()).isEqualTo(OperationStatus.REJECTED);
				rejections++;
			}
		}

		assertThat(successes + rejections + conflicts).isEqualTo(50);
		BigDecimal expectedBalance = new BigDecimal("1000.00")
				.subtract(new BigDecimal("100.00").multiply(BigDecimal.valueOf(successes)));
		BigDecimal balance = balanceOf(account.getId());
		assertThat(balance).isEqualByComparingTo(expectedBalance);
		assertThat(balance.signum()).isGreaterThanOrEqualTo(0);
		assertThat(countByAccount(account.getId())).isEqualTo(successes + rejections);
	}

}
