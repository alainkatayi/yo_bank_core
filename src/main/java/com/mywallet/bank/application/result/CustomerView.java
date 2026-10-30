package com.mywallet.bank.application.result;

import java.time.Instant;
import java.util.UUID;

public record CustomerView(
		UUID id,
		String firstName,
		String lastName,
		String phone,
		String email,
		String status,
		Instant createdAt
) {
}
