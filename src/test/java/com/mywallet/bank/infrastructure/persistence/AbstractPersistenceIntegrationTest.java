package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Customer;
import com.mywallet.bank.domain.model.CustomerStatus;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.port.AccountRepository;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
abstract class AbstractPersistenceIntegrationTest {

	@ServiceConnection
	static final PostgreSQLContainer POSTGRES = startedPostgres();

	private static PostgreSQLContainer startedPostgres() {
		PostgreSQLContainer container = new PostgreSQLContainer("postgres:16");
		container.start();
		return container;
	}

	private static final AtomicInteger SEQUENCE = new AtomicInteger();

	@Autowired
	protected CustomerRepository customerRepository;

	@Autowired
	protected AccountRepository accountRepository;

	protected Customer newCustomer() {
		int sequence = SEQUENCE.incrementAndGet();
		return new Customer(
				UUID.randomUUID(),
				"Amina",
				"Kabila",
				"243" + String.format("%09d", sequence),
				"amina." + sequence + "@example.com",
				CustomerStatus.ACTIVE,
				Instant.parse("2026-01-15T08:30:00Z")
		);
	}

	protected BankAccount newAccount(UUID customerId, Money balance, AccountStatus status) {
		return new BankAccount(
				UUID.randomUUID(),
				new AccountNumber(nextAccountNumber()),
				customerId,
				balance,
				status,
				0L,
				Instant.parse("2026-01-15T09:00:00Z")
		);
	}

	protected String nextAccountNumber() {
		return String.format("%012d", SEQUENCE.incrementAndGet());
	}

	protected BankAccount persistedAccount(Money balance) {
		Customer customer = customerRepository.save(newCustomer());
		return accountRepository.save(newAccount(customer.getId(), balance, AccountStatus.ACTIVE));
	}

}
