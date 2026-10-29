package com.mywallet.bank.application.exception;

public final class ConcurrentUpdateException extends RuntimeException {

	private final String reference;

	public ConcurrentUpdateException(String reference, Throwable cause) {
		super("Conflit de concurrence non résolu pour la référence : " + reference, cause);
		this.reference = reference;
	}

	public String getReference() {
		return reference;
	}

}
