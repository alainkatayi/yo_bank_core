package com.mywallet.bank.application.command;

public record AccountStatusCommand(AdminActor actor, String accountNumber) {

	public AccountStatusCommand {
		if (actor == null) {
			throw new IllegalArgumentException("L'acteur est obligatoire");
		}
	}

}
