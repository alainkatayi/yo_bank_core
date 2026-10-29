package com.mywallet.bank.application.service;

import com.mywallet.bank.application.result.AccountView;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Customer;

final class AccountViews {

	private AccountViews() {
	}

	static AccountView from(BankAccount account, Customer customer) {
		return new AccountView(
				account.getAccountNumber().value(),
				mask(customer.getFirstName(), customer.getLastName()),
				account.getBalance(),
				account.getStatus()
		);
	}

	static String mask(String firstName, String lastName) {
		return initial(firstName) + " " + initial(lastName);
	}

	private static String initial(String value) {
		int firstCodePoint = value.codePointAt(0);
		return new String(Character.toChars(firstCodePoint)) + "***";
	}

}
