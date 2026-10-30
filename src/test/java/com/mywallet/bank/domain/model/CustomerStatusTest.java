package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerStatusTest {

	@Test
	void suspendsAndReactivates() {
		Customer customer = customer(CustomerStatus.ACTIVE);

		customer.suspend();
		assertThat(customer.getStatus()).isEqualTo(CustomerStatus.SUSPENDED);

		customer.reactivate();
		assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
	}

	@Test
	void refusesASecondSuspensionAndAReactivationOfAnActiveCustomer() {
		Customer active = customer(CustomerStatus.ACTIVE);
		assertThatThrownBy(active::reactivate).isInstanceOf(InvalidStateTransitionException.class);

		Customer suspended = customer(CustomerStatus.SUSPENDED);
		assertThatThrownBy(suspended::suspend).isInstanceOf(InvalidStateTransitionException.class);
	}

	private static Customer customer(CustomerStatus status) {
		return new Customer(
				UUID.randomUUID(),
				"Amina",
				"Kabila",
				"243810000001",
				"amina@example.com",
				status,
				Instant.parse("2026-01-15T10:00:00Z")
		);
	}

}
