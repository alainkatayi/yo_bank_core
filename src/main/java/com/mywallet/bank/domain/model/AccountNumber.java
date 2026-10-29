package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.InvalidAccountNumberException;

import java.util.regex.Pattern;

public record AccountNumber(String value) {

	private static final Pattern TWELVE_DIGITS = Pattern.compile("^\\d{12}$");

	public AccountNumber {
		if (value == null || !TWELVE_DIGITS.matcher(value).matches()) {
			throw new InvalidAccountNumberException(
					"Le numéro de compte doit contenir exactement 12 chiffres"
			);
		}
	}

}
