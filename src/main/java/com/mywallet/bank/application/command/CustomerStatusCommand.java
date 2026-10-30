package com.mywallet.bank.application.command;

import java.util.UUID;

public record CustomerStatusCommand(AdminActor actor, UUID customerId) {

	public CustomerStatusCommand {
		if (actor == null || customerId == null) {
			throw new IllegalArgumentException("L'acteur et le client sont obligatoires");
		}
	}

}
