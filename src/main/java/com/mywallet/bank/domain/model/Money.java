package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) implements Comparable<Money> {

	public static final String CURRENCY = "CDF";

	public Money {
		if (amount == null) {
			throw new InvalidAmountException("Le montant est obligatoire");
		}
		if (amount.signum() < 0) {
			throw new InvalidAmountException("Le montant ne peut pas être négatif : " + amount);
		}
		try {
			amount = amount.setScale(2, RoundingMode.UNNECESSARY);
		} catch (ArithmeticException exception) {
			throw new InvalidAmountException(
					"Le montant doit avoir au plus 2 décimales : " + amount,
					exception
			);
		}
	}

	public static Money of(String value) {
		if (value == null || value.isBlank()) {
			throw new InvalidAmountException("Le montant est obligatoire");
		}
		try {
			return new Money(new BigDecimal(value));
		} catch (NumberFormatException exception) {
			throw new InvalidAmountException("Montant invalide : " + value, exception);
		}
	}

	public static Money of(BigDecimal value) {
		return new Money(value);
	}

	public static Money zero() {
		return new Money(BigDecimal.ZERO);
	}

	public Money add(Money other) {
		if (other == null) {
			throw new InvalidAmountException("Le montant à ajouter est obligatoire");
		}
		return new Money(amount.add(other.amount));
	}

	public Money subtract(Money other) {
		if (other == null) {
			throw new InvalidAmountException("Le montant à soustraire est obligatoire");
		}
		if (amount.compareTo(other.amount) < 0) {
			throw new InvalidAmountException("La soustraction donnerait un montant négatif");
		}
		return new Money(amount.subtract(other.amount));
	}

	public boolean isPositive() {
		return amount.signum() > 0;
	}

	public boolean isZero() {
		return amount.signum() == 0;
	}

	public boolean isLessThan(Money other) {
		return compareTo(other) < 0;
	}

	public boolean isGreaterThanOrEqual(Money other) {
		return compareTo(other) >= 0;
	}

	@Override
	public int compareTo(Money other) {
		if (other == null) {
			throw new InvalidAmountException("Le montant à comparer est obligatoire");
		}
		return amount.compareTo(other.amount);
	}

}
