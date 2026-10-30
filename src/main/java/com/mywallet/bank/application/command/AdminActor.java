package com.mywallet.bank.application.command;

public record AdminActor(String id, String username) {

	public AdminActor {
		if (id == null || id.isBlank() || id.length() > 64) {
			throw new IllegalArgumentException("L'identifiant de l'acteur est obligatoire");
		}
		if (username == null || username.isBlank() || username.length() > 100) {
			throw new IllegalArgumentException("Le nom de l'acteur est obligatoire");
		}
	}

}
