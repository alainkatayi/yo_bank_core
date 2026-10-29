package com.mywallet.bank.application.exception;

public final class AccountNotFoundException extends RuntimeException {

	private final String accountNumber;

	public AccountNotFoundException(String accountNumber) {
		super("Compte introuvable : " + accountNumber);
		this.accountNumber = accountNumber;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

}
