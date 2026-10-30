package com.mywallet.bank.api;

public record AccountResponse(
		String accountNumber,
		String holderName,
		String balance,
		String status
) {
}
