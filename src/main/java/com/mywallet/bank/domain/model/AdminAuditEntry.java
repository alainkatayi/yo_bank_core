package com.mywallet.bank.domain.model;

import java.time.Instant;
import java.util.UUID;

public final class AdminAuditEntry {

	private final UUID id;
	private final String actorId;
	private final String actorUsername;
	private final AuditAction action;
	private final AuditTargetType targetType;
	private final String targetId;
	private final String details;
	private final Instant createdAt;

	public AdminAuditEntry(
			UUID id,
			String actorId,
			String actorUsername,
			AuditAction action,
			AuditTargetType targetType,
			String targetId,
			String details,
			Instant createdAt
	) {
		if (id == null) {
			throw new IllegalArgumentException("L'identifiant d'audit est obligatoire");
		}
		this.actorId = requireBounded(actorId, 64, "L'identifiant de l'acteur");
		this.actorUsername = requireBounded(actorUsername, 100, "Le nom de l'acteur");
		if (action == null || targetType == null) {
			throw new IllegalArgumentException("L'action et le type de cible sont obligatoires");
		}
		this.targetId = requireBounded(targetId, 64, "L'identifiant de la cible");
		if (details != null && details.isBlank()) {
			throw new IllegalArgumentException("Le détail d'audit ne peut pas être vide");
		}
		if (createdAt == null) {
			throw new IllegalArgumentException("La date d'audit est obligatoire");
		}
		this.id = id;
		this.action = action;
		this.targetType = targetType;
		this.details = details;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public String getActorId() {
		return actorId;
	}

	public String getActorUsername() {
		return actorUsername;
	}

	public AuditAction getAction() {
		return action;
	}

	public AuditTargetType getTargetType() {
		return targetType;
	}

	public String getTargetId() {
		return targetId;
	}

	public String getDetails() {
		return details;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	private static String requireBounded(String value, int max, String field) {
		if (value == null || value.isBlank() || value.length() > max) {
			throw new IllegalArgumentException(field + " est obligatoire");
		}
		return value;
	}

}
