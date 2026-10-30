package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.AccountNotEmptyException;
import com.mywallet.bank.domain.exception.InvalidAmountException;
import com.mywallet.bank.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankAccountTest {

	private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:00:00Z");

	@Test
	void debitsWhenBalanceIsSufficient() {
		BankAccount account = account(Money.of("50.00"), AccountStatus.ACTIVE);

		AccountMovementResult result = account.debit(Money.of("10.00"));

		assertThat(result).isEqualTo(new AccountMovementResult.Accepted(Money.of("40.00")));
		assertThat(account.getBalance()).isEqualTo(Money.of("40.00"));
		assertThat(account.getVersion()).isZero();
	}

	@Test
	void debitsExactBalanceDownToZero() {
		BankAccount account = account(Money.of("10.00"), AccountStatus.ACTIVE);

		AccountMovementResult result = account.debit(Money.of("10.00"));

		assertThat(result).isEqualTo(new AccountMovementResult.Accepted(Money.zero()));
		assertThat(account.getBalance().isZero()).isTrue();
		assertThat(account.getBalance()).isEqualTo(Money.of("0.00"));
	}

	@Test
	void rejectsDebitWhenFundsAreInsufficientAndLeavesBalanceUnchanged() {
		BankAccount account = account(Money.of("10.00"), AccountStatus.ACTIVE);

		AccountMovementResult result = account.debit(Money.of("10.01"));

		assertThat(result).isEqualTo(new AccountMovementResult.Rejected(RejectionReason.INSUFFICIENT_FUNDS));
		assertThat(account.getBalance()).isEqualTo(Money.of("10.00"));
	}

	@Test
	void creditsAnActiveAccount() {
		BankAccount account = account(Money.of("10.00"), AccountStatus.ACTIVE);

		AccountMovementResult result = account.credit(Money.of("5.50"));

		assertThat(result).isEqualTo(new AccountMovementResult.Accepted(Money.of("15.50")));
		assertThat(account.getBalance()).isEqualTo(Money.of("15.50"));
	}

	@Test
	void rejectsDebitAndCreditWhenAccountIsBlocked() {
		BankAccount debitAccount = account(Money.of("10.00"), AccountStatus.BLOCKED);
		BankAccount creditAccount = account(Money.of("10.00"), AccountStatus.BLOCKED);

		assertThat(debitAccount.debit(Money.of("1.00")))
				.isEqualTo(new AccountMovementResult.Rejected(RejectionReason.ACCOUNT_BLOCKED));
		assertThat(creditAccount.credit(Money.of("1.00")))
				.isEqualTo(new AccountMovementResult.Rejected(RejectionReason.ACCOUNT_BLOCKED));
		assertThat(debitAccount.getBalance()).isEqualTo(Money.of("10.00"));
		assertThat(creditAccount.getBalance()).isEqualTo(Money.of("10.00"));
	}

	@Test
	void rejectsDebitAndCreditWhenAccountIsClosed() {
		BankAccount debitAccount = account(Money.of("10.00"), AccountStatus.CLOSED);
		BankAccount creditAccount = account(Money.of("10.00"), AccountStatus.CLOSED);

		assertThat(debitAccount.debit(Money.of("1.00")))
				.isEqualTo(new AccountMovementResult.Rejected(RejectionReason.ACCOUNT_CLOSED));
		assertThat(creditAccount.credit(Money.of("1.00")))
				.isEqualTo(new AccountMovementResult.Rejected(RejectionReason.ACCOUNT_CLOSED));
		assertThat(debitAccount.getBalance()).isEqualTo(Money.of("10.00"));
		assertThat(creditAccount.getBalance()).isEqualTo(Money.of("10.00"));
	}

	@Test
	void rejectsZeroOrNegativeAmount() {
		BankAccount account = account(Money.of("10.00"), AccountStatus.ACTIVE);

		assertThatThrownBy(() -> account.debit(Money.zero()))
				.isInstanceOf(InvalidAmountException.class);
		assertThatThrownBy(() -> account.credit(Money.zero()))
				.isInstanceOf(InvalidAmountException.class);
		assertThatThrownBy(() -> account.debit(Money.of("-1.00")))
				.isInstanceOf(InvalidAmountException.class);
		assertThatThrownBy(() -> account.credit(Money.of("-5")))
				.isInstanceOf(InvalidAmountException.class);
		assertThat(account.getBalance()).isEqualTo(Money.of("10.00"));
	}

	@Test
	void blocksAnActiveAccountAndRejectsLaterMovements() {
		BankAccount account = account(Money.of("10.00"), AccountStatus.ACTIVE);

		account.block();

		assertThat(account.getStatus()).isEqualTo(AccountStatus.BLOCKED);
		assertThat(account.debit(Money.of("1.00")))
				.isEqualTo(new AccountMovementResult.Rejected(RejectionReason.ACCOUNT_BLOCKED));
		assertThat(account.credit(Money.of("1.00")))
				.isEqualTo(new AccountMovementResult.Rejected(RejectionReason.ACCOUNT_BLOCKED));
		assertThat(account.getBalance()).isEqualTo(Money.of("10.00"));
	}

	@Test
	void unblocksOnlyABlockedAccount() {
		BankAccount account = account(Money.of("10.00"), AccountStatus.BLOCKED);

		account.unblock();

		assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(account.debit(Money.of("1.00")))
				.isEqualTo(new AccountMovementResult.Accepted(Money.of("9.00")));
	}

	@Test
	void closesOnlyWhenTheBalanceIsZeroAndRefusesReopening() {
		BankAccount funded = account(Money.of("10.00"), AccountStatus.ACTIVE);
		assertThatThrownBy(funded::close).isInstanceOf(AccountNotEmptyException.class);
		assertThat(funded.getStatus()).isEqualTo(AccountStatus.ACTIVE);

		BankAccount empty = account(Money.zero(), AccountStatus.BLOCKED);
		empty.close();
		assertThat(empty.getStatus()).isEqualTo(AccountStatus.CLOSED);
		assertThat(empty.debit(Money.of("1.00")))
				.isEqualTo(new AccountMovementResult.Rejected(RejectionReason.ACCOUNT_CLOSED));
		assertThatThrownBy(empty::unblock).isInstanceOf(InvalidStateTransitionException.class);
		assertThatThrownBy(empty::block).isInstanceOf(InvalidStateTransitionException.class);
		assertThatThrownBy(empty::close).isInstanceOf(InvalidStateTransitionException.class);
	}

	@Test
	void refusesABlockWhenTheAccountIsNotActive() {
		BankAccount account = account(Money.zero(), AccountStatus.BLOCKED);
		assertThatThrownBy(account::block).isInstanceOf(InvalidStateTransitionException.class);
	}

	private static BankAccount account(Money balance, AccountStatus status) {
		return new BankAccount(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				new AccountNumber("123456789012"),
				UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
				balance,
				status,
				0L,
				CREATED_AT
		);
	}

}
