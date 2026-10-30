package com.mywallet.bank.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgentResponse(
		String id,
		String username,
		String firstName,
		String lastName,
		String email,
		String role,
		boolean enabled,
		Instant createdAt
) {
}
