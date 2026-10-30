package com.mywallet.bank.application.result;

import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.AuditTargetType;

import java.time.Instant;

public record AuditEntryView(
		String actorId,
		String actorUsername,
		AuditAction action,
		AuditTargetType targetType,
		String targetId,
		String details,
		Instant createdAt
) {
}
