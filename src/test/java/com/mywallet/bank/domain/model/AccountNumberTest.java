package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.InvalidAccountNumberException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountNumberTest {

	@Test
	void acceptsExactlyTwelveDigits() {
		AccountNumber number = new AccountNumber("123456789012");

		assertThat(number.value()).isEqualTo("123456789012");
	}

	@Test
	void rejectsTooShort() {
		assertThatThrownBy(() -> new AccountNumber("12345678901"))
				.isInstanceOf(InvalidAccountNumberException.class);
	}

	@Test
	void rejectsTooLong() {
		assertThatThrownBy(() -> new AccountNumber("1234567890123"))
				.isInstanceOf(InvalidAccountNumberException.class);
	}

	@Test
	void rejectsLetters() {
		assertThatThrownBy(() -> new AccountNumber("12345678901A"))
				.isInstanceOf(InvalidAccountNumberException.class);
		assertThatThrownBy(() -> new AccountNumber("abcdefghijkl"))
				.isInstanceOf(InvalidAccountNumberException.class);
	}

}
