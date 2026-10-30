package com.mywallet.bank.domain.exception;

public final class InvalidStateTransitionException extends RuntimeException {

	public InvalidStateTransitionException() {
		super("Transition de statut interdite");
	}

}
