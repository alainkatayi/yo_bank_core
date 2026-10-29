package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.CustomerStatus;
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
@Table(name = "customer")
public class CustomerJpaEntity implements Persistable<UUID> {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@Column(name = "phone", nullable = false, unique = true, length = 20)
	private String phone;

	@Column(name = "email", length = 150)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private CustomerStatus status;

	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Transient
	private boolean newEntity = true;

	protected CustomerJpaEntity() {
	}

	CustomerJpaEntity(
			UUID id,
			String firstName,
			String lastName,
			String phone,
			String email,
			CustomerStatus status,
			Instant createdAt
	) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.email = email;
		this.status = status;
		this.createdAt = createdAt;
	}

	void apply(String firstName, String lastName, String phone, String email, CustomerStatus status) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.email = email;
		this.status = status;
	}

	@Override
	public UUID getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public CustomerStatus getStatus() {
		return status;
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
