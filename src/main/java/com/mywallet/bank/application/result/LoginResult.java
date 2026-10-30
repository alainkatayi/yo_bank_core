package com.mywallet.bank.application.result;

public record LoginResult(IssuedToken token, AgentView agent) {

	public LoginResult {
		if (token == null || agent == null) {
			throw new IllegalArgumentException("Le résultat de connexion est incomplet");
		}
	}

}
