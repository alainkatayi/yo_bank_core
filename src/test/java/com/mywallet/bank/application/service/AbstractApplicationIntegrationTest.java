package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.port.in.CreditAccountUseCase;
import com.mywallet.bank.application.port.in.DebitAccountUseCase;
import com.mywallet.bank.application.port.in.GetAccountUseCase;
import com.mywallet.bank.application.port.in.GetOperationUseCase;
import com.mywallet.bank.application.result.OperationResult;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
abstract class AbstractApplicationIntegrationTest {

	@ServiceConnection
	static final PostgreSQLContainer POSTGRES = startedPostgres();

	private static final AtomicInteger SEQUENCE = new AtomicInteger();

	@Autowired
	protected DebitAccountUseCase debitAccountUseCase;

	@Autowired
	protected CreditAccountUseCase creditAccountUseCase;

	@Autowired
	protected GetOperationUseCase getOperationUseCase;

	@Autowired
	protected GetAccountUseCase getAccountUseCase;

	@Autowired
	protected CustomerRepository customerRepository;

	@Autowired
	protected AccountRepository accountRepository;

	@Autowired
	protected JdbcTemplate jdbcTemplate;

	private static PostgreSQLContainer startedPostgres() {
		PostgreSQLContainer container = new PostgreSQLContainer("postgres:16");
		container.start();
		return container;
	}

	protected BankAccount persistedAccount(Money balance) {
		int sequence = SEQUENCE.incrementAndGet();
		Customer customer = customerRepository.save(new Customer(
				UUID.randomUUID(),
				"Amina",
				"Kabila",
				"243" + String.format("%09d", sequence),
				"amina." + sequence + "@example.com",
				CustomerStatus.ACTIVE,
				Instant.parse("2026-01-15T08:30:00Z")
		));
		return accountRepository.save(new BankAccount(
				UUID.randomUUID(),
				new AccountNumber(String.format("%012d", SEQUENCE.incrementAndGet())),
				customer.getId(),
				balance,
				AccountStatus.ACTIVE,
				0L,
				Instant.parse("2026-01-15T09:00:00Z")
		));
	}

	protected int countByReference(String reference) {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from bank_operation where reference = ?",
				Integer.class,
				reference
		);
		return count == null ? 0 : count;
	}

	protected int countByAccount(UUID accountId) {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from bank_operation where account_id = ?",
				Integer.class,
				accountId
		);
		return count == null ? 0 : count;
	}

	protected BigDecimal balanceOf(UUID accountId) {
		return jdbcTemplate.queryForObject(
				"select balance from bank_account where id = ?",
				BigDecimal.class,
				accountId
		);
	}

	protected List<AttemptOutcome> runTogether(List<Callable<OperationResult>> tasks) throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(tasks.size());
		CountDownLatch ready = new CountDownLatch(tasks.size());
		CountDownLatch start = new CountDownLatch(1);
		try {
			List<Future<OperationResult>> futures = new ArrayList<>();
			for (Callable<OperationResult> task : tasks) {
				futures.add(pool.submit(() -> {
					ready.countDown();
					start.await();
					return task.call();
				}));
			}
			ready.await();
			start.countDown();
			List<AttemptOutcome> outcomes = new ArrayList<>();
			for (Future<OperationResult> future : futures) {
				try {
					outcomes.add(AttemptOutcome.completed(future.get(3, TimeUnit.MINUTES)));
				} catch (ExecutionException exception) {
					outcomes.add(AttemptOutcome.failed(exception.getCause()));
				} catch (TimeoutException exception) {
					throw new IllegalStateException("Une tentative parallèle n'a pas abouti", exception);
				}
			}
			return outcomes;
		} finally {
			pool.shutdownNow();
		}
	}

	protected record AttemptOutcome(OperationResult result, Throwable error) {

		static AttemptOutcome completed(OperationResult result) {
			return new AttemptOutcome(result, null);
		}

		static AttemptOutcome failed(Throwable error) {
			return new AttemptOutcome(null, error);
		}

	}

}
