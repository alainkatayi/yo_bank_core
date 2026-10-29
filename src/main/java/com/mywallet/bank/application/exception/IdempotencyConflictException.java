package com.mywallet.bank.application.exception;

public final class IdempotencyConflictException extends RuntimeException {

	private final String reference;

	public IdempotencyConflictException(String reference) {
		super("La référence est déjà utilisée pour une autre opération : " + reference);
		this.reference = reference;
	}

	public String getReference() {
		return reference;
	}

}
