package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.LinkStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "link_request")
public class LinkRequestJpaEntity implements Persistable<UUID> {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "account_number", nullable = false, length = 12, updatable = false)
	private String accountNumber;

	@Column(name = "account_id")
	private UUID accountId;

	@Column(name = "eligible", nullable = false, updatable = false)
	private boolean eligible;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "code_hash", length = 64, updatable = false)
	private String codeHash;

	@Column(name = "attempts", nullable = false)
	private int attempts;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 12)
	private LinkStatus status;

	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	@Column(name = "expires_at", nullable = false, updatable = false)
	private Instant expiresAt;

	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	@Column(name = "confirmed_at")
	private Instant confirmedAt;

	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Transient
	private boolean newEntity = true;

	protected LinkRequestJpaEntity() {
	}

	LinkRequestJpaEntity(
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

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return newEntity;
	}

	@PostLoad
	@PostPersist
	private void markPersisted() {
		this.newEntity = false;
	}

	String getAccountNumber() {
		return accountNumber;
	}

	UUID getAccountId() {
		return accountId;
	}

	boolean isEligible() {
		return eligible;
	}

	String getCodeHash() {
		return codeHash;
	}

	int getAttempts() {
		return attempts;
	}

	LinkStatus getStatus() {
		return status;
	}

	Instant getExpiresAt() {
		return expiresAt;
	}

	Instant getConfirmedAt() {
		return confirmedAt;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

}
