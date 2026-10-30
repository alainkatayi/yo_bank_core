package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.InvalidStateTransitionException;

import java.time.Instant;
import java.util.UUID;

public final class Customer {

	private final UUID id;
	private final String firstName;
	private final String lastName;
	private final String phone;
	private final String email;
	private CustomerStatus status;
	private final Instant createdAt;

	public Customer(
			UUID id,
			String firstName,
			String lastName,
			String phone,
			String email,
			CustomerStatus status,
			Instant createdAt
	) {
		if (id == null) {
			throw new IllegalArgumentException("L'identifiant client est obligatoire");
		}
		this.firstName = requireText(firstName, "Le prénom");
		this.lastName = requireText(lastName, "Le nom");
		this.phone = requireText(phone, "Le téléphone");
		if (email != null && email.isBlank()) {
			throw new IllegalArgumentException("L'e-mail ne peut pas être vide");
		}
		if (status == null) {
			throw new IllegalArgumentException("Le statut client est obligatoire");
		}
		if (createdAt == null) {
			throw new IllegalArgumentException("La date de création est obligatoire");
		}
		this.id = id;
		this.email = email;
		this.status = status;
		this.createdAt = createdAt;
	}

	/**
	 * La suspension ne bloque pas les comptes du client.
	 * Chaque compte garde son statut : un débit n'est refusé pour blocage
	 * que si le compte lui-même est BLOCKED ou CLOSED.
	 */
	public void suspend() {
		if (status != CustomerStatus.ACTIVE) {
			throw new InvalidStateTransitionException();
		}
		status = CustomerStatus.SUSPENDED;
	}

	public void reactivate() {
		if (status != CustomerStatus.SUSPENDED) {
			throw new InvalidStateTransitionException();
		}
		status = CustomerStatus.ACTIVE;
	}

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

	private static String requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " est obligatoire");
		}
		return value;
	}

}
