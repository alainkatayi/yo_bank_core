package com.mywallet.bank.application.command;

import java.util.UUID;

public record OpenAccountCommand(AdminActor actor, UUID customerId) {

	public OpenAccountCommand {
		if (actor == null) {
			throw new IllegalArgumentException("L'acteur est obligatoire");
		}
		if (customerId == null) {
			throw new IllegalArgumentException("L'identifiant client est obligatoire");
		}
	}

}
