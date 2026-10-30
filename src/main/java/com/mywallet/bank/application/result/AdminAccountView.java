package com.mywallet.bank.application.result;

import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.Money;

public record AdminAccountView(String accountNumber, Money balance, AccountStatus status) {

	public AdminAccountView {
		if (accountNumber == null || accountNumber.isBlank() || balance == null || status == null) {
			throw new IllegalArgumentException("Le compte admin est incomplet");
		}
	}

}
