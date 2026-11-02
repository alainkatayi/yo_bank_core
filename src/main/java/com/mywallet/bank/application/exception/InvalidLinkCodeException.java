package com.mywallet.bank.application.exception;

public final class InvalidLinkCodeException extends RuntimeException {

	public InvalidLinkCodeException() {
		super("Code de liaison invalide");
	}

}
