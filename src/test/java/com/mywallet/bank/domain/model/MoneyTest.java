package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

	@Test
	void createsValidAmount() {
		assertThat(Money.of("10.00")).isEqualTo(Money.of(new BigDecimal("10.00")));
		assertThat(Money.of("10.00").amount()).isEqualByComparingTo("10.00");
		assertThat(Money.zero().isZero()).isTrue();
		assertThat(Money.zero().isPositive()).isFalse();
		assertThat(Money.of("0.01").isPositive()).isTrue();
		assertThat(Money.CURRENCY).isEqualTo("CDF");
	}

	@Test
	void normalizesToTwoDecimals() {
		assertThat(Money.of("10").amount()).isEqualTo(new BigDecimal("10.00"));
		assertThat(Money.of("10.5").amount()).isEqualTo(new BigDecimal("10.50"));
		assertThat(Money.of(new BigDecimal("10")).amount().scale()).isEqualTo(2);
		assertThat(Money.of("10")).isEqualTo(Money.of("10.00"));
	}

	@Test
	void rejectsMoreThanTwoSignificantDecimals() {
		assertThatThrownBy(() -> Money.of("10.005"))
				.isInstanceOf(InvalidAmountException.class);
		assertThatThrownBy(() -> Money.of(new BigDecimal("10.005")))
				.isInstanceOf(InvalidAmountException.class);
	}

	@Test
	void rejectsNegativeAndBlankAmounts() {
		assertThatThrownBy(() -> Money.of("-0.01"))
				.isInstanceOf(InvalidAmountException.class);
		assertThatThrownBy(() -> Money.of(new BigDecimal("-1")))
				.isInstanceOf(InvalidAmountException.class);
		assertThatThrownBy(() -> Money.of("   "))
				.isInstanceOf(InvalidAmountException.class);
		assertThatThrownBy(() -> Money.of((String) null))
				.isInstanceOf(InvalidAmountException.class);
		assertThatThrownBy(() -> Money.of((BigDecimal) null))
				.isInstanceOf(InvalidAmountException.class);
	}

	@Test
	void addsAndSubtracts() {
		assertThat(Money.of("10.00").add(Money.of("2.50"))).isEqualTo(Money.of("12.50"));
		assertThat(Money.of("10.00").subtract(Money.of("2.50"))).isEqualTo(Money.of("7.50"));
		assertThat(Money.of("10.00").subtract(Money.of("10")).isZero()).isTrue();

		assertThat(Money.of("1.00").isLessThan(Money.of("2.00"))).isTrue();
		assertThat(Money.of("2.00").isGreaterThanOrEqual(Money.of("2.00"))).isTrue();
		assertThat(Money.of("2.00").isGreaterThanOrEqual(Money.of("1.50"))).isTrue();
		assertThat(Money.of("1").compareTo(Money.of("1.00"))).isZero();
	}

	@Test
	void rejectsSubtractThatWouldBeNegative() {
		Money original = Money.of("5.00");

		assertThatThrownBy(() -> original.subtract(Money.of("5.01")))
				.isInstanceOf(InvalidAmountException.class);
		assertThat(original).isEqualTo(Money.of("5.00"));
	}

}
