package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.AdminAuditCommand;
import com.mywallet.bank.domain.model.AdminAuditEntry;
import com.mywallet.bank.domain.port.AuditLogRepository;

import java.time.Clock;
import java.util.UUID;

final class Audits {

	private Audits() {
	}

	static void append(AuditLogRepository auditLog, AdminAuditCommand command, Clock clock) {
		auditLog.save(new AdminAuditEntry(
				UUID.randomUUID(),
				command.actor().id(),
				command.actor().username(),
				command.action(),
				command.targetType(),
				command.targetId(),
				command.details(),
				Timestamps.now(clock)
		));
	}

}
