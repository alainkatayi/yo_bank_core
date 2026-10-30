package com.mywallet.bank.application.exception;

public final class DuplicatePhoneException extends RuntimeException {

	public DuplicatePhoneException() {
		super("Ce téléphone est déjà utilisé");
	}

}
