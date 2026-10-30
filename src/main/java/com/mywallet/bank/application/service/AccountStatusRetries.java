package com.mywallet.bank.application.service;

import com.mywallet.bank.application.RetryProperties;
import com.mywallet.bank.application.command.AccountStatusCommand;
import com.mywallet.bank.application.exception.ConcurrentUpdateException;
import com.mywallet.bank.application.result.AdminAccountView;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Ne jamais appeler ce bean depuis une transaction déjà ouverte.
 * Chaque tentative de {@link AccountStatusExecutor} ouvre puis ferme la sienne.
 */
@Service
public class AccountStatusRetries {

	private final AccountStatusExecutor executor;
	private final RetryProperties retryProperties;

	public AccountStatusRetries(AccountStatusExecutor executor, RetryProperties retryProperties) {
		this.executor = executor;
		this.retryProperties = retryProperties;
	}

	AdminAccountView change(AccountStatusCommand command, AccountStatusChange change) {
		if (command == null) {
			throw new IllegalArgumentException("La commande est obligatoire");
		}
		RuntimeException lastFailure = null;
		int maxAttempts = retryProperties.maxAttempts();
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				return executor.apply(command, change);
			} catch (OptimisticLockingFailureException exception) {
				lastFailure = exception;
			}
			if (attempt < maxAttempts) {
				pause();
			}
		}
		throw new ConcurrentUpdateException(command.accountNumber(), lastFailure);
	}

	private static void pause() {
		try {
			Thread.sleep(ThreadLocalRandom.current().nextInt(1, 16));
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new ConcurrentUpdateException("account-status", exception);
		}
	}

}
