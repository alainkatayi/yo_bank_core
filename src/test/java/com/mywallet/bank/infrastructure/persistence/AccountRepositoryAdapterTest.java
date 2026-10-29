package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountRepositoryAdapterTest extends AbstractPersistenceIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void savesAndReadsAccountByIdAndAccountNumber() {
		CustomerHolder holder = savedCustomer();
		BankAccount account = newAccount(holder.id(), Money.of("150.50"), AccountStatus.BLOCKED);

		BankAccount saved = accountRepository.save(account);

		assertThat(saved.getBalance()).isEqualTo(Money.of("150.50"));
		assertThat(saved.getStatus()).isEqualTo(AccountStatus.BLOCKED);
		assertThat(saved.getVersion()).isZero();
		assertThat(saved.getAccountNumber()).isEqualTo(account.getAccountNumber());
		assertThat(saved.getCustomerId()).isEqualTo(holder.id());
		assertThat(saved.getCreatedAt()).isEqualTo(account.getCreatedAt());
		assertThat(accountRepository.findById(account.getId()).orElseThrow())
				.usingRecursiveComparison()
				.isEqualTo(saved);
		assertThat(accountRepository.findByAccountNumber(account.getAccountNumber()).orElseThrow())
				.usingRecursiveComparison()
				.isEqualTo(saved);
	}

	@Test
	void incrementsVersionAfterABalanceChange() {
		BankAccount created = persistedAccount(Money.of("100.00"));
		assertThat(created.getVersion()).isZero();
		created.debit(Money.of("25.00"));

		BankAccount updated = accountRepository.save(created);

		assertThat(updated.getVersion()).isEqualTo(1L);
		assertThat(updated.getBalance()).isEqualTo(Money.of("75.00"));
		BankAccount reloaded = accountRepository.findById(created.getId()).orElseThrow();
		assertThat(reloaded.getVersion()).isEqualTo(1L);
		assertThat(reloaded.getBalance()).isEqualTo(Money.of("75.00"));
		assertThat(reloaded.getStatus()).isEqualTo(AccountStatus.ACTIVE);
	}

	@Test
	void rejectsTheSecondSaveWhenTwoCopiesDiverge() {
		BankAccount created = persistedAccount(Money.of("100.00"));
		BankAccount first = accountRepository.findById(created.getId()).orElseThrow();
		BankAccount second = accountRepository.findById(created.getId()).orElseThrow();
		first.debit(Money.of("10.00"));
		second.debit(Money.of("40.00"));

		accountRepository.save(first);

		assertThatThrownBy(() -> accountRepository.save(second))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);
		BankAccount current = accountRepository.findById(created.getId()).orElseThrow();
		assertThat(current.getBalance()).isEqualTo(Money.of("90.00"));
		assertThat(current.getVersion()).isEqualTo(1L);
	}

	@Test
	void rejectsANegativeBalanceInsertedDirectlyInTheDatabase() {
		CustomerHolder holder = savedCustomer();

		assertThatThrownBy(() -> jdbcTemplate.update(
				"""
				INSERT INTO bank_account (
				    id, account_number, customer_id, balance, currency, status, version, created_at
				) VALUES (?, ?, ?, ?, 'CDF', 'ACTIVE', 0, ?)
				""",
				UUID.randomUUID(),
				nextAccountNumber(),
				holder.id(),
				new BigDecimal("-1.00"),
				Timestamp.from(Instant.parse("2026-01-15T09:00:00Z"))
		))
				.isInstanceOf(DataIntegrityViolationException.class)
				.rootCause()
				.hasMessageContaining("balance");
	}

	private CustomerHolder savedCustomer() {
		return new CustomerHolder(customerRepository.save(newCustomer()).getId());
	}

	private record CustomerHolder(UUID id) {
	}

}
