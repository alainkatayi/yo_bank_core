package com.mywallet.bank.application.command;

import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.AuditTargetType;

public record AdminAuditCommand(
		AdminActor actor,
		AuditAction action,
		AuditTargetType targetType,
		String targetId,
		String details
) {
}
