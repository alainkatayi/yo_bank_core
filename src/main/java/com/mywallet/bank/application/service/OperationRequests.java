package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.domain.exception.InvalidAmountException;
import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.Money;

final class OperationRequests {

	private OperationRequests() {
	}

	static Parsed parse(OperationCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("La commande est obligatoire");
		}
		String reference = command.reference();
		if (reference == null || reference.isBlank() || reference.length() > 64) {
			throw new IllegalArgumentException("La référence est obligatoire et limitée à 64 caractères");
		}
		AccountNumber accountNumber = new AccountNumber(command.accountNumber());
		Money amount = Money.of(command.amount());
		if (!amount.isPositive()) {
			throw new InvalidAmountException("Le montant de l'opération doit être strictement positif");
		}
		return new Parsed(reference, accountNumber, amount);
	}

	record Parsed(String reference, AccountNumber accountNumber, Money amount) {
	}

}
