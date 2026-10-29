package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.exception.AccountNotFoundException;
import com.mywallet.bank.application.result.AccountView;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationApplicationIntegrationTest extends AbstractApplicationIntegrationTest {

	@Test
	void debitsAndCreditsThenReadsTheOperationAndTheBalance() {
		BankAccount account = persistedAccount(Money.of("200.00"));
		OperationCommand debit = new OperationCommand("FLOW-DEBIT-" + UUID.randomUUID(), account.getAccountNumber().value(), "50.00");
		OperationCommand credit = new OperationCommand("FLOW-CREDIT-" + UUID.randomUUID(), account.getAccountNumber().value(), "20.00");

		OperationResult debited = debitAccountUseCase.execute(debit);
		OperationResult credited = creditAccountUseCase.execute(credit);

		assertThat(debited.type()).isEqualTo(OperationType.DEBIT);
		assertThat(debited.status()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(debited.balanceAfter()).isEqualTo(Money.of("150.00"));
		assertThat(credited.type()).isEqualTo(OperationType.CREDIT);
		assertThat(credited.status()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(credited.balanceAfter()).isEqualTo(Money.of("170.00"));
		assertThat(getOperationUseCase.execute(debit.reference())).isEqualTo(debited);
		assertThat(getOperationUseCase.execute(credit.reference())).isEqualTo(credited);

		AccountView view = getAccountUseCase.execute(account.getAccountNumber().value());
		assertThat(view.accountNumber()).isEqualTo(account.getAccountNumber().value());
		assertThat(view.maskedHolderName()).isEqualTo("A*** K***");
		assertThat(view.balance()).isEqualTo(Money.of("170.00"));
		assertThat(view.status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(balanceOf(account.getId())).isEqualByComparingTo("170.00");
		assertThat(accountRepository.findById(account.getId()).orElseThrow().getVersion()).isEqualTo(2L);
	}

	@Test
	void replayingAReferenceKeepsASingleMovement() {
		BankAccount account = persistedAccount(Money.of("100.00"));
		OperationCommand command = new OperationCommand(
				"REPLAY-" + UUID.randomUUID(),
				account.getAccountNumber().value(),
				"30.00"
		);

		OperationResult first = debitAccountUseCase.execute(command);
		OperationResult second = debitAccountUseCase.execute(command);

		assertThat(second).isEqualTo(first);
		assertThat(countByReference(command.reference())).isEqualTo(1);
		assertThat(countByAccount(account.getId())).isEqualTo(1);
		assertThat(balanceOf(account.getId())).isEqualByComparingTo("70.00");
		assertThat(accountRepository.findById(account.getId()).orElseThrow().getVersion()).isEqualTo(1L);
	}

	@Test
	void persistsNothingWhenTheAccountIsUnknown() {
		String reference = "MISSING-" + UUID.randomUUID();

		assertThatThrownBy(() -> debitAccountUseCase.execute(new OperationCommand(reference, "000000000000", "10.00")))
				.isInstanceOf(AccountNotFoundException.class);
		assertThat(countByReference(reference)).isZero();
	}

}
