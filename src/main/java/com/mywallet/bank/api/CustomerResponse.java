package com.mywallet.bank.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomerResponse(
		UUID id,
		String firstName,
		String lastName,
		String phone,
		String email,
		String status,
		Instant createdAt
) {
}
