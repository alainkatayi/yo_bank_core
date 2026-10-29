package com.mywallet.bank.domain.exception;

public final class InvalidAccountNumberException extends RuntimeException {

	public InvalidAccountNumberException(String message) {
		super(message);
	}

}
