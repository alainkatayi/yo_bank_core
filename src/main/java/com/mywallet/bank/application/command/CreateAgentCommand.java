package com.mywallet.bank.application.command;

import com.mywallet.bank.domain.model.AgentRole;

public record CreateAgentCommand(
		AdminActor actor,
		String username,
		String password,
		String firstName,
		String lastName,
		String email,
		AgentRole role
) {

	public CreateAgentCommand {
		if (actor == null || role == null) {
			throw new IllegalArgumentException("La commande agent est incomplète");
		}
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Le mot de passe est obligatoire");
		}
	}

}
