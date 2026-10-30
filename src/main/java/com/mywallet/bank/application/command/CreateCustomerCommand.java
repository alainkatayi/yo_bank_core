package com.mywallet.bank.application.command;

public record CreateCustomerCommand(
		AdminActor actor,
		String firstName,
		String lastName,
		String phone,
		String email
) {

	public CreateCustomerCommand {
		if (actor == null) {
			throw new IllegalArgumentException("L'acteur est obligatoire");
		}
	}

}
