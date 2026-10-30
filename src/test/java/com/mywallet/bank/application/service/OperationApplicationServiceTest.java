package com.mywallet.bank.application.service;

import com.mywallet.bank.application.RetryProperties;
import com.mywallet.bank.application.command.AdminAuditCommand;
import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.exception.ConcurrentUpdateException;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationApplicationServiceTest {

	private static final OperationCommand COMMAND = new OperationCommand("REF-RETRY", "123456789012", "10.00");
	private static final OperationResult SUCCESS = new OperationResult(
			"REF-RETRY",
			OperationType.DEBIT,
			OperationStatus.SUCCESS,
			null,
			Money.of("90.00"),
			Instant.parse("2026-04-01T12:00:00Z")
	);

	@Test
	void retriesAnOptimisticLockThenReturnsTheAttempt() {
		Queue<Object> script = new ArrayDeque<>();
		script.add(new ObjectOptimisticLockingFailureException(Object.class, UUID.randomUUID()));
		script.add(SUCCESS);
		ScriptedExecutor executor = new ScriptedExecutor(script);
		OperationApplicationService service = service(executor, 5);

		assertThat(service.execute(COMMAND, OperationType.DEBIT)).isEqualTo(SUCCESS);
		assertThat(executor.calls).isEqualTo(2);
	}

	@Test
	void retriesADuplicateReferenceThenAppliesTheNextAttempt() {
		Queue<Object> script = new ArrayDeque<>();
		script.add(duplicateReference());
		script.add(SUCCESS);
		ScriptedExecutor executor = new ScriptedExecutor(script);
		OperationApplicationService service = service(executor, 5);

		assertThat(service.execute(COMMAND, OperationType.DEBIT)).isEqualTo(SUCCESS);
		assertThat(executor.calls).isEqualTo(2);
	}

	@Test
	void throwsWhenTheAttemptsAreExhausted() {
		Queue<Object> script = new ArrayDeque<>();
		script.add(new ObjectOptimisticLockingFailureException(Object.class, "1"));
		script.add(new ObjectOptimisticLockingFailureException(Object.class, "2"));
		script.add(new ObjectOptimisticLockingFailureException(Object.class, "3"));
		ScriptedExecutor executor = new ScriptedExecutor(script);
		OperationApplicationService service = service(executor, 3);

		assertThatThrownBy(() -> service.execute(COMMAND, OperationType.DEBIT))
				.isInstanceOf(ConcurrentUpdateException.class)
				.hasCauseInstanceOf(ObjectOptimisticLockingFailureException.class);
		assertThat(executor.calls).isEqualTo(3);
	}

	@Test
	void doesNotRetryAForeignIntegrityViolation() {
		ScriptedExecutor executor = new ScriptedExecutor(new ArrayDeque<>());
		executor.script.add(new DataIntegrityViolationException("autre contrainte"));
		OperationApplicationService service = service(executor, 5);

		assertThatThrownBy(() -> service.execute(COMMAND, OperationType.DEBIT))
				.isInstanceOf(DataIntegrityViolationException.class);
		assertThat(executor.calls).isEqualTo(1);
	}

	private static OperationApplicationService service(ScriptedExecutor executor, int maxAttempts) {
		return new OperationApplicationService(executor, new RetryProperties(maxAttempts));
	}

	private static DataIntegrityViolationException duplicateReference() {
		return new DataIntegrityViolationException(
				"duplicate",
				new SQLException(
						"duplicate key value violates unique constraint \"bank_operation_reference_key\"",
						"23505"
				)
		);
	}

	private static final class ScriptedExecutor extends OperationExecutor {

		private final Queue<Object> script;
		private int calls;

		private ScriptedExecutor(Queue<Object> script) {
			super(
					new InMemoryAccountRepository(),
					new InMemoryOperationRepository(),
					new InMemoryAuditLogRepository(),
					java.time.Clock.systemUTC()
			);
			this.script = script;
		}

		@Override
		public OperationResult execute(OperationCommand command, OperationType type, AdminAuditCommand audit) {
			calls++;
			Object next = script.remove();
			if (next instanceof RuntimeException exception) {
				throw exception;
			}
			return (OperationResult) next;
		}

	}

}
