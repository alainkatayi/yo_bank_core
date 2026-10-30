package com.mywallet.bank.application.service;

final class AccountNumberTakenException extends RuntimeException {

	AccountNumberTakenException() {
		super("Collision de numéro de compte");
	}

}
