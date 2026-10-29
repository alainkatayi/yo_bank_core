package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.InvalidAmountException;

import java.time.Instant;
import java.util.UUID;

public final class BankOperation {

	private final UUID id;
	private final String reference;
	private final UUID accountId;
	private final OperationType type;
	private final Money amount;
	private final OperationStatus status;
	private final RejectionReason rejectionReason;
	private final Money balanceAfter;
	private final Instant createdAt;

	public static BankOperation success(
			UUID id,
			String reference,
			UUID accountId,
			OperationType type,
			Money amount,
			Money balanceAfter,
			Instant createdAt
	) {
		return new BankOperation(
				id,
				reference,
				accountId,
				type,
				amount,
				OperationStatus.SUCCESS,
				null,
				balanceAfter,
				createdAt
		);
	}

	public static BankOperation rejected(
			UUID id,
			String reference,
			UUID accountId,
			OperationType type,
			Money amount,
			RejectionReason rejectionReason,
			Instant createdAt
	) {
		return new BankOperation(
				id,
				reference,
				accountId,
				type,
				amount,
				OperationStatus.REJECTED,
				rejectionReason,
				null,
				createdAt
		);
	}

	BankOperation(
			UUID id,
			String reference,
			UUID accountId,
			OperationType type,
			Money amount,
			OperationStatus status,
			RejectionReason rejectionReason,
			Money balanceAfter,
			Instant createdAt
	) {
		if (id == null) {
			throw new IllegalArgumentException("L'identifiant de l'opération est obligatoire");
		}
		if (reference == null || reference.isBlank() || reference.length() > 64) {
			throw new IllegalArgumentException("La référence est obligatoire et limitée à 64 caractères");
		}
		if (accountId == null) {
			throw new IllegalArgumentException("L'identifiant du compte est obligatoire");
		}
		if (type == null) {
			throw new IllegalArgumentException("Le type d'opération est obligatoire");
		}
		if (amount == null || !amount.isPositive()) {
			throw new InvalidAmountException("Le montant de l'opération doit être strictement positif");
		}
		if (status == null) {
			throw new IllegalArgumentException("Le statut de l'opération est obligatoire");
		}
		if (createdAt == null) {
			throw new IllegalArgumentException("La date de création est obligatoire");
		}
		if (status == OperationStatus.SUCCESS && (balanceAfter == null || rejectionReason != null)) {
			throw new IllegalArgumentException(
					"Une opération réussie exige le solde après opération et aucun motif de rejet"
			);
		}
		if (status == OperationStatus.REJECTED && rejectionReason == null) {
			throw new IllegalArgumentException("Une opération rejetée exige un motif de rejet");
		}
		this.id = id;
		this.reference = reference;
		this.accountId = accountId;
		this.type = type;
		this.amount = amount;
		this.status = status;
		this.rejectionReason = rejectionReason;
		this.balanceAfter = balanceAfter;
		this.createdAt = createdAt;
	}

	public boolean isSameRequestAs(UUID accountId, OperationType type, Money amount) {
		return this.accountId.equals(accountId) && this.type == type && this.amount.equals(amount);
	}

	public UUID getId() {
		return id;
	}

	public String getReference() {
		return reference;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public OperationType getType() {
		return type;
	}

	public Money getAmount() {
		return amount;
	}

	public OperationStatus getStatus() {
		return status;
	}

	public RejectionReason getRejectionReason() {
		return rejectionReason;
	}

	public Money getBalanceAfter() {
		return balanceAfter;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
