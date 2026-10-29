package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.Customer;
import com.mywallet.bank.domain.model.CustomerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerRepositoryAdapterTest extends AbstractPersistenceIntegrationTest {

	@Test
	void savesAndReadsCustomerByIdAndPhone() {
		Customer customer = newCustomer();

		Customer saved = customerRepository.save(customer);

		assertThat(customerRepository.findById(customer.getId()).orElseThrow())
				.usingRecursiveComparison()
				.isEqualTo(saved);
		assertThat(customerRepository.findByPhone(customer.getPhone()).orElseThrow())
				.usingRecursiveComparison()
				.isEqualTo(saved);
		assertThat(saved.getId()).isEqualTo(customer.getId());
		assertThat(saved.getFirstName()).isEqualTo("Amina");
		assertThat(saved.getLastName()).isEqualTo("Kabila");
		assertThat(saved.getPhone()).isEqualTo(customer.getPhone());
		assertThat(saved.getEmail()).isEqualTo(customer.getEmail());
		assertThat(saved.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
		assertThat(saved.getCreatedAt()).isEqualTo(Instant.parse("2026-01-15T08:30:00Z"));
	}

	@Test
	void rejectsADuplicatePhone() {
		Customer existing = customerRepository.save(newCustomer());
		Customer duplicate = new Customer(
				UUID.randomUUID(),
				"Grace",
				"Hopper",
				existing.getPhone(),
				"grace.hopper@example.com",
				CustomerStatus.SUSPENDED,
				Instant.parse("2026-02-01T00:00:00Z")
		);

		assertThatThrownBy(() -> customerRepository.save(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
		assertThat(customerRepository.findByPhone(existing.getPhone()).orElseThrow().getId())
				.isEqualTo(existing.getId());
	}

}
