package com.mywallet.bank.application.result;

import com.mywallet.bank.domain.model.AgentRole;

import java.time.Instant;
import java.util.UUID;

public record AgentView(
		UUID id,
		String username,
		String firstName,
		String lastName,
		String email,
		AgentRole role,
		boolean enabled,
		Instant createdAt
) {
}
