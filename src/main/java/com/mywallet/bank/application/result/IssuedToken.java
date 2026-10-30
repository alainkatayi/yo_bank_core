package com.mywallet.bank.application.result;

public record IssuedToken(String value, long expiresInSeconds) {

	public IssuedToken {
		if (value == null || value.isBlank() || expiresInSeconds < 1) {
			throw new IllegalArgumentException("Le jeton émis est incomplet");
		}
	}

}
