package com.mywallet.bank.application.exception;

public final class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Identifiants invalides");
	}

}
