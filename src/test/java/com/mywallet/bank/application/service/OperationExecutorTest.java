package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.exception.AccountNotFoundException;
import com.mywallet.bank.application.exception.IdempotencyConflictException;
import com.mywallet.bank.domain.exception.InvalidAmountException;
import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.model.RejectionReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationExecutorTest {

	private static final Instant NOW = Instant.parse("2026-04-01T12:00:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

	private InMemoryAccountRepository accounts;
	private InMemoryOperationRepository operations;
	private OperationExecutor executor;

	@BeforeEach
	void setUp() {
		accounts = new InMemoryAccountRepository();
		operations = new InMemoryOperationRepository();
		executor = new OperationExecutor(accounts, operations, new InMemoryAuditLogRepository(), CLOCK);
	}

	@Test
	void debitsAnActiveAccount() {
		BankAccount account = store(Money.of("100.00"), AccountStatus.ACTIVE);
		OperationCommand command = command("REF-DEBIT", account, "30.00");

		var result = executor.execute(command, OperationType.DEBIT);

		assertThat(result.reference()).isEqualTo("REF-DEBIT");
		assertThat(result.type()).isEqualTo(OperationType.DEBIT);
		assertThat(result.status()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(result.rejectionReason()).isNull();
		assertThat(result.balanceAfter()).isEqualTo(Money.of("70.00"));
		assertThat(result.createdAt()).isEqualTo(NOW);
		assertThat(accounts.saveCount()).isEqualTo(1);
		assertThat(operations.insertCount()).isEqualTo(1);
		assertThat(accounts.only().getBalance()).isEqualTo(Money.of("70.00"));
	}

	@Test
	void persistsARejectedDebitAndLeavesTheAccountUntouched() {
		BankAccount account = store(Money.of("10.00"), AccountStatus.ACTIVE);

		var result = executor.execute(command("REF-FUNDS", account, "50.00"), OperationType.DEBIT);

		assertThat(result.status()).isEqualTo(OperationStatus.REJECTED);
		assertThat(result.rejectionReason()).isEqualTo(RejectionReason.INSUFFICIENT_FUNDS);
		assertThat(result.balanceAfter()).isNull();
		assertThat(result.type()).isEqualTo(OperationType.DEBIT);
		assertThat(accounts.saveCount()).isZero();
		assertThat(operations.insertCount()).isEqualTo(1);
		assertThat(accounts.only().getBalance()).isEqualTo(Money.of("10.00"));
	}

	@Test
	void creditsAnActiveAccount() {
		BankAccount account = store(Money.of("100.00"), AccountStatus.ACTIVE);

		var result = executor.execute(command("REF-CREDIT", account, "15.50"), OperationType.CREDIT);

		assertThat(result.status()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(result.type()).isEqualTo(OperationType.CREDIT);
		assertThat(result.balanceAfter()).isEqualTo(Money.of("115.50"));
		assertThat(result.rejectionReason()).isNull();
		assertThat(accounts.saveCount()).isEqualTo(1);
		assertThat(accounts.only().getBalance()).isEqualTo(Money.of("115.50"));
	}

	@Test
	void rejectsABlockedAccountWithoutChangingIt() {
		BankAccount account = store(Money.of("80.00"), AccountStatus.BLOCKED);

		var result = executor.execute(command("REF-BLOCKED", account, "10.00"), OperationType.DEBIT);

		assertThat(result.status()).isEqualTo(OperationStatus.REJECTED);
		assertThat(result.rejectionReason()).isEqualTo(RejectionReason.ACCOUNT_BLOCKED);
		assertThat(result.balanceAfter()).isNull();
		assertThat(accounts.saveCount()).isZero();
		assertThat(operations.insertCount()).isEqualTo(1);
		assertThat(accounts.only().getBalance()).isEqualTo(Money.of("80.00"));
		assertThat(accounts.only().getStatus()).isEqualTo(AccountStatus.BLOCKED);
	}

	@Test
	void persistsNothingWhenTheAccountDoesNotExist() {
		OperationCommand command = new OperationCommand("REF-MISSING", "123456789012", "10.00");

		assertThatThrownBy(() -> executor.execute(command, OperationType.DEBIT))
				.isInstanceOf(AccountNotFoundException.class);
		assertThat(accounts.saveCount()).isZero();
		assertThat(operations.insertCount()).isZero();
	}

	@Test
	void replaysTheSameReferenceWithoutANewMovement() {
		BankAccount account = store(Money.of("100.00"), AccountStatus.ACTIVE);
		OperationCommand command = command("REF-SAME", account, "25.00");

		var first = executor.execute(command, OperationType.DEBIT);
		var second = executor.execute(command, OperationType.DEBIT);

		assertThat(second).isEqualTo(first);
		assertThat(operations.insertCount()).isEqualTo(1);
		assertThat(accounts.saveCount()).isEqualTo(1);
		assertThat(accounts.only().getBalance()).isEqualTo(Money.of("75.00"));
	}

	@Test
	void rejectsTheSameReferenceWhenTheAmountDiffers() {
		BankAccount account = store(Money.of("100.00"), AccountStatus.ACTIVE);
		executor.execute(command("REF-CONFLICT", account, "10.00"), OperationType.DEBIT);

		assertThatThrownBy(() -> executor.execute(command("REF-CONFLICT", account, "20.00"), OperationType.DEBIT))
				.isInstanceOf(IdempotencyConflictException.class);
		assertThat(operations.insertCount()).isEqualTo(1);
		assertThat(accounts.saveCount()).isEqualTo(1);
		assertThat(accounts.only().getBalance()).isEqualTo(Money.of("90.00"));
	}

	@Test
	void rejectsAnInvalidAmountWithoutPersisting() {
		store(Money.of("100.00"), AccountStatus.ACTIVE);

		assertThatThrownBy(() -> executor.execute(
				new OperationCommand("REF-NEG", "123456789012", "-1.00"),
				OperationType.DEBIT
		)).isInstanceOf(InvalidAmountException.class);
		assertThatThrownBy(() -> executor.execute(
				new OperationCommand("REF-ZERO", "123456789012", "0.00"),
				OperationType.DEBIT
		)).isInstanceOf(InvalidAmountException.class);
		assertThat(accounts.saveCount()).isZero();
		assertThat(operations.insertCount()).isZero();
		assertThat(accounts.only().getBalance()).isEqualTo(Money.of("100.00"));
	}

	private BankAccount store(Money balance, AccountStatus status) {
		BankAccount account = new BankAccount(
				UUID.randomUUID(),
				new AccountNumber("123456789012"),
				UUID.randomUUID(),
				balance,
				status,
				0L,
				NOW
		);
		accounts.store(account);
		return account;
	}

	private static OperationCommand command(String reference, BankAccount account, String amount) {
		return new OperationCommand(reference, account.getAccountNumber().value(), amount);
	}

}
