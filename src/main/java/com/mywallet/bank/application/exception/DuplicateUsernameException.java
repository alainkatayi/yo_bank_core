package com.mywallet.bank.application.exception;

public final class DuplicateUsernameException extends RuntimeException {

	public DuplicateUsernameException() {
		super("Ce nom d'utilisateur est déjà utilisé");
	}

}
