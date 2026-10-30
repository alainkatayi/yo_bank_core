package com.mywallet.bank.application.exception;

public final class InvalidQueryException extends RuntimeException {

	public InvalidQueryException() {
		super("La pagination est invalide");
	}

}
