package com.mywallet.bank.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class BankAgent {

	private final UUID id;
	private final String username;
	private final String passwordHash;
	private final String firstName;
	private final String lastName;
	private final String email;
	private final AgentRole role;
	private final boolean enabled;
	private final Instant createdAt;

	public BankAgent(
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
		if (id == null) {
			throw new IllegalArgumentException("L'identifiant de l'agent est obligatoire");
		}
		this.username = requireText(username, 100, "Le nom d'utilisateur");
		this.passwordHash = requireText(passwordHash, 100, "L'empreinte du mot de passe");
		this.firstName = requireText(firstName, 100, "Le prénom");
		this.lastName = requireText(lastName, 100, "Le nom");
		if (email != null && (email.isBlank() || email.length() > 150)) {
			throw new IllegalArgumentException("L'e-mail de l'agent est invalide");
		}
		if (role == null) {
			throw new IllegalArgumentException("Le rôle de l'agent est obligatoire");
		}
		if (createdAt == null) {
			throw new IllegalArgumentException("La date de création est obligatoire");
		}
		this.id = id;
		this.email = email;
		this.role = role;
		this.enabled = enabled;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public AgentRole getRole() {
		return role;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	private static String requireText(String value, int max, String label) {
		if (value == null || value.isBlank() || value.length() > max) {
			throw new IllegalArgumentException(label + " est obligatoire");
		}
		return value;
	}

}
