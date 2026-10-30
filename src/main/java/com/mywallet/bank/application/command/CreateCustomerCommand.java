package com.mywallet.bank.application.command;

public record CreateCustomerCommand(
		String firstName,
		String lastName,
		String phone,
		String email
) {
}
