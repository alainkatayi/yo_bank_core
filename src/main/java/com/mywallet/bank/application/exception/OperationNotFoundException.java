package com.mywallet.bank.application.exception;

public final class OperationNotFoundException extends RuntimeException {

	private final String reference;

	public OperationNotFoundException(String reference) {
		super("Opération introuvable : " + reference);
		this.reference = reference;
	}

	public String getReference() {
		return reference;
	}

}
