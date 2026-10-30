package com.mywallet.bank.application.exception;

public final class CustomerNotFoundException extends RuntimeException {

	public CustomerNotFoundException() {
		super("Client introuvable");
	}

}
