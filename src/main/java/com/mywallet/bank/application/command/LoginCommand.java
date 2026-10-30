package com.mywallet.bank.application.command;

public record LoginCommand(String username, String password) {

	public LoginCommand {
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			throw new IllegalArgumentException("Les identifiants sont obligatoires");
		}
	}

}
