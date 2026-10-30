package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.AgentRole;
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
@Table(name = "bank_agent")
public class BankAgentJpaEntity implements Persistable<UUID> {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "username", nullable = false, unique = true, length = 100)
	private String username;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Column(name = "email", length = 150)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	private AgentRole role;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;

	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Transient
	private boolean newEntity = true;

	protected BankAgentJpaEntity() {
	}

	BankAgentJpaEntity(
			UUID id,
			String username,
			String passwordHash,
			String firstName,
			String lastName,
			String email,
			AgentRole role,
			boolean enabled,
			Instant createdAt
	) {
		this.id = id;
		this.username = username;
		this.passwordHash = passwordHash;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.role = role;
		this.enabled = enabled;
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

	String getUsername() {
		return username;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	String getFirstName() {
		return firstName;
	}

	String getLastName() {
		return lastName;
	}

	String getEmail() {
		return email;
	}

	AgentRole getRole() {
		return role;
	}

	boolean isEnabled() {
		return enabled;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

}
