package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.AuditTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLogJpaEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "actor_id", nullable = false, updatable = false, length = 64)
	private String actorId;

	@Column(name = "actor_username", nullable = false, updatable = false, length = 100)
	private String actorUsername;

	@Enumerated(EnumType.STRING)
	@Column(name = "action", nullable = false, updatable = false, length = 40)
	private AuditAction action;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, updatable = false, length = 20)
	private AuditTargetType targetType;

	@Column(name = "target_id", nullable = false, updatable = false, length = 64)
	private String targetId;

	@Column(name = "details", updatable = false)
	private String details;

	@JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected AdminAuditLogJpaEntity() {
	}

	AdminAuditLogJpaEntity(
			UUID id,
			String actorId,
			String actorUsername,
			AuditAction action,
			AuditTargetType targetType,
			String targetId,
			String details,
			Instant createdAt
	) {
		this.id = id;
		this.actorId = actorId;
		this.actorUsername = actorUsername;
		this.action = action;
		this.targetType = targetType;
		this.targetId = targetId;
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

}
