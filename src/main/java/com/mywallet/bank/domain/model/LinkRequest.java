package com.mywallet.bank.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class LinkRequest {

	private final UUID id;
	private final String accountNumber;
	private final UUID accountId;
	private final boolean eligible;
	private final String codeHash;
	private final int attempts;
	private final LinkStatus status;
	private final Instant expiresAt;
	private final Instant confirmedAt;
	private final Instant createdAt;

	public LinkRequest(
			UUID id,
			String accountNumber,
			UUID accountId,
			boolean eligible,
			String codeHash,
			int attempts,
			LinkStatus status,
			Instant expiresAt,
			Instant confirmedAt,
			Instant createdAt
	) {
		if (id == null) {
			throw new IllegalArgumentException("L'identifiant de liaison est obligatoire");
		}
		if (accountNumber == null || !accountNumber.matches("\\d{12}")) {
			throw new IllegalArgumentException("Le numéro de compte de la liaison est invalide");
		}
		if (eligible && (codeHash == null || !codeHash.matches("[0-9a-f]{64}"))) {
			throw new IllegalArgumentException("Une demande éligible exige l'empreinte du code");
		}
		if (!eligible && codeHash != null) {
			throw new IllegalArgumentException("Une demande non éligible n'a pas de code");
		}
		if (attempts < 0) {
			throw new IllegalArgumentException("Le nombre d'essais ne peut pas être négatif");
		}
		if (status == null || expiresAt == null || createdAt == null) {
			throw new IllegalArgumentException("La demande de liaison est incomplète");
		}
		this.id = id;
		this.accountNumber = accountNumber;
		this.accountId = accountId;
		this.eligible = eligible;
		this.codeHash = codeHash;
		this.attempts = attempts;
		this.status = status;
		this.expiresAt = expiresAt;
		this.confirmedAt = confirmedAt;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public boolean isEligible() {
		return eligible;
	}

	public String getCodeHash() {
		return codeHash;
	}

	public int getAttempts() {
		return attempts;
	}

	public LinkStatus getStatus() {
		return status;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getConfirmedAt() {
		return confirmedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
