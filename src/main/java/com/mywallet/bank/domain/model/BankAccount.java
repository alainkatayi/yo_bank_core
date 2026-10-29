package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.InvalidAmountException;

import java.time.Instant;
import java.util.UUID;

public final class BankAccount {

	private final UUID id;
	private final AccountNumber accountNumber;
	private final UUID customerId;
	private Money balance;
	private final AccountStatus status;
	private final long version;
	private final Instant createdAt;

	public BankAccount(
			UUID id,
			AccountNumber accountNumber,
			UUID customerId,
			Money balance,
			AccountStatus status,
			long version,
			Instant createdAt
	) {
		if (id == null) {
			throw new IllegalArgumentException("L'identifiant du compte est obligatoire");
		}
		if (accountNumber == null) {
			throw new IllegalArgumentException("Le numéro de compte est obligatoire");
		}
		if (customerId == null) {
			throw new IllegalArgumentException("L'identifiant client est obligatoire");
		}
		if (balance == null) {
			throw new IllegalArgumentException("Le solde est obligatoire");
		}
		if (status == null) {
			throw new IllegalArgumentException("Le statut du compte est obligatoire");
		}
		if (version < 0) {
			throw new IllegalArgumentException("La version ne peut pas être négative");
		}
		if (createdAt == null) {
			throw new IllegalArgumentException("La date de création est obligatoire");
		}
		this.id = id;
		this.accountNumber = accountNumber;
		this.customerId = customerId;
		this.balance = balance;
		this.status = status;
		this.version = version;
		this.createdAt = createdAt;
	}

	public AccountMovementResult debit(Money amount) {
		requireStrictlyPositive(amount);
		AccountMovementResult refusal = refuseIfNotActive();
		if (refusal != null) {
			return refusal;
		}
		if (balance.isLessThan(amount)) {
			return new AccountMovementResult.Rejected(RejectionReason.INSUFFICIENT_FUNDS);
		}
		balance = balance.subtract(amount);
		return new AccountMovementResult.Accepted(balance);
	}

	public AccountMovementResult credit(Money amount) {
		requireStrictlyPositive(amount);
		AccountMovementResult refusal = refuseIfNotActive();
		if (refusal != null) {
			return refusal;
		}
		balance = balance.add(amount);
		return new AccountMovementResult.Accepted(balance);
	}

	public UUID getId() {
		return id;
	}

	public AccountNumber getAccountNumber() {
		return accountNumber;
	}

	public UUID getCustomerId() {
		return customerId;
	}

	public Money getBalance() {
		return balance;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public long getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	private AccountMovementResult refuseIfNotActive() {
		if (status == AccountStatus.BLOCKED) {
			return new AccountMovementResult.Rejected(RejectionReason.ACCOUNT_BLOCKED);
		}
		if (status == AccountStatus.CLOSED) {
			return new AccountMovementResult.Rejected(RejectionReason.ACCOUNT_CLOSED);
		}
		return null;
	}

	private static void requireStrictlyPositive(Money amount) {
		if (amount == null || !amount.isPositive()) {
			throw new InvalidAmountException("Le montant de l'opération doit être strictement positif");
		}
	}

}
