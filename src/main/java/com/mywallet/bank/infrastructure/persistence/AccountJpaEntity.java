package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bank_account")
public class AccountJpaEntity implements Persistable<UUID> {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "account_number", nullable = false, unique = true, length = 12, updatable = false)
	private String accountNumber;

	@Column(name = "customer_id", nullable = false, updatable = false)
	private UUID customerId;

	@Column(name = "balance", nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "currency", nullable = false, length = 3, updatable = false)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private AccountStatus status;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Transient
	private boolean newEntity = true;

	protected AccountJpaEntity() {
	}

	AccountJpaEntity(
			UUID id,
			String accountNumber,
			UUID customerId,
			BigDecimal balance,
			AccountStatus status,
			Instant createdAt
	) {
		this.id = id;
		this.accountNumber = accountNumber;
		this.customerId = customerId;
		this.balance = balance;
		this.currency = Money.CURRENCY;
		this.status = status;
		this.createdAt = createdAt;
	}

	void apply(BigDecimal balance, AccountStatus status) {
		this.balance = balance;
		this.status = status;
	}

	@Override
	public UUID getId() {
		return id;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public UUID getCustomerId() {
		return customerId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public String getCurrency() {
		return currency;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public Long getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
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

}
