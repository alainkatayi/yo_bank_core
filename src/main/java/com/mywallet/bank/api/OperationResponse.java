package com.mywallet.bank.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OperationResponse(
		String reference,
		String type,
		String status,
		String rejectionReason,
		String balanceAfter,
		Instant createdAt
) {
}
