package com.mywallet.bank.application.result;

import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.Money;

public record AccountView(
		String accountNumber,
		String maskedHolderName,
		Money balance,
		AccountStatus status
) {

	public AccountView {
		if (accountNumber == null || accountNumber.isBlank()) {
			throw new IllegalArgumentException("Le numéro de compte est obligatoire");
		}
		if (maskedHolderName == null || maskedHolderName.isBlank()) {
			throw new IllegalArgumentException("Le nom masqué est obligatoire");
		}
		if (balance == null || status == null) {
			throw new IllegalArgumentException("Le solde et le statut sont obligatoires");
		}
	}

}
