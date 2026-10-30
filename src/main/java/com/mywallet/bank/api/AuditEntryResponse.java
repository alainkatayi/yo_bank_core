package com.mywallet.bank.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditEntryResponse(
		String actorId,
		String actorUsername,
		String action,
		String targetType,
		String targetId,
		String details,
		Instant createdAt
) {
}
