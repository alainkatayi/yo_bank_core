package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.model.RejectionReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bank_operation")
public class OperationJpaEntity implements Persistable<UUID> {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "reference", nullable = false, unique = true, length = 64, updatable = false)
	private String reference;

	@Column(name = "account_id", nullable = false, updatable = false)
	private UUID accountId;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 10, updatable = false)
	private OperationType type;

	@Column(name = "amount", nullable = false, precision = 19, scale = 2, updatable = false)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 10, updatable = false)
	private OperationStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "rejection_reason", length = 30, updatable = false)
	private RejectionReason rejectionReason;

	@Column(name = "balance_after", precision = 19, scale = 2, updatable = false)
	private BigDecimal balanceAfter;

	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected OperationJpaEntity() {
	}

	OperationJpaEntity(
			UUID id,
			String reference,
			UUID accountId,
			OperationType type,
			BigDecimal amount,
			OperationStatus status,
			RejectionReason rejectionReason,
			BigDecimal balanceAfter,
			Instant createdAt
	) {
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

	@Override
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

	public BigDecimal getAmount() {
		return amount;
	}

	public OperationStatus getStatus() {
		return status;
	}

	public RejectionReason getRejectionReason() {
		return rejectionReason;
	}

	public BigDecimal getBalanceAfter() {
		return balanceAfter;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	@Override
	public boolean isNew() {
		return true;
	}

}
