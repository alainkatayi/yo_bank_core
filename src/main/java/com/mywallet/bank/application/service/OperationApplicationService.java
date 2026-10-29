package com.mywallet.bank.application.service;

import com.mywallet.bank.application.RetryProperties;
import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.exception.ConcurrentUpdateException;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.OperationType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ne jamais appeler ce bean depuis une transaction déjà ouverte.
 * Le retry s'exécute ici, hors transaction : chaque appel à {@link OperationExecutor}
 * ouvre puis ferme la sienne. Si cet appel rejoignait une transaction externe déjà
 * condamnée au rollback, la relecture d'idempotence serait invalide.
 */
@Service
public class OperationApplicationService {

	private final OperationExecutor operationExecutor;
	private final RetryProperties retryProperties;

	public OperationApplicationService(OperationExecutor operationExecutor, RetryProperties retryProperties) {
		this.operationExecutor = operationExecutor;
		this.retryProperties = retryProperties;
	}

	public OperationResult execute(OperationCommand command, OperationType type) {
		OperationRequests.parse(command);
		RuntimeException lastFailure = null;
		int maxAttempts = retryProperties.maxAttempts();
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				return operationExecutor.execute(command, type);
			} catch (OptimisticLockingFailureException exception) {
				lastFailure = exception;
			} catch (DataIntegrityViolationException exception) {
				if (!isDuplicateReference(exception)) {
					throw exception;
				}
				lastFailure = exception;
			}
			if (attempt < maxAttempts) {
				pauseBeforeRetry(command.reference());
			}
		}
		throw new ConcurrentUpdateException(command.reference(), lastFailure);
	}

	private static boolean isDuplicateReference(DataIntegrityViolationException exception) {
		Throwable current = exception;
		while (current != null) {
			if (current instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
				String message = sql.getMessage() == null ? "" : sql.getMessage();
				return message.contains("reference");
			}
			String message = current.getMessage();
			if (message != null && message.contains("bank_operation_reference_key")) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private static void pauseBeforeRetry(String reference) {
		try {
			Thread.sleep(ThreadLocalRandom.current().nextInt(1, 16));
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new ConcurrentUpdateException(reference, exception);
		}
	}

}
