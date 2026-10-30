package com.mywallet.bank.application.exception;

public final class CustomerSuspendedException extends RuntimeException {

	public CustomerSuspendedException() {
		super("Le client est suspendu");
	}

}
