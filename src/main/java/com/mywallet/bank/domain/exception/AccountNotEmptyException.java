package com.mywallet.bank.domain.exception;

public final class AccountNotEmptyException extends RuntimeException {

	public AccountNotEmptyException() {
		super("Le solde doit être nul pour clôturer le compte");
	}

}
