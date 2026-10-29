package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.exception.AccountNotFoundException;
import com.mywallet.bank.application.exception.IdempotencyConflictException;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.AccountMovementResult;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.BankOperation;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.port.AccountRepository;
import com.mywallet.bank.domain.port.OperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class OperationExecutor {

	private final AccountRepository accounts;
	private final OperationRepository operations;
	private final Clock clock;

	public OperationExecutor(AccountRepository accounts, OperationRepository operations, Clock clock) {
		this.accounts = accounts;
		this.operations = operations;
		this.clock = clock;
	}

	/**
	 * Une seule tentative. Aucun retry ici : un conflit doit sortir de la transaction
	 * pour que l'appelant puisse en ouvrir une nouvelle.
	 */
	@Transactional
	public OperationResult execute(OperationCommand command, OperationType type) {
		if (type == null) {
			throw new IllegalArgumentException("Le type d'opération est obligatoire");
		}
		OperationRequests.Parsed request = OperationRequests.parse(command);
		BankOperation existing = operations.findByReference(request.reference()).orElse(null);
		BankAccount account = accounts.findByAccountNumber(request.accountNumber()).orElse(null);
		if (existing != null) {
			if (account != null && existing.isSameRequestAs(account.getId(), type, request.amount())) {
				return OperationResults.from(existing);
			}
			throw new IdempotencyConflictException(request.reference());
		}
		if (account == null) {
			throw new AccountNotFoundException(request.accountNumber().value());
		}
		AccountMovementResult movement = switch (type) {
			case DEBIT -> account.debit(request.amount());
			case CREDIT -> account.credit(request.amount());
		};
		UUID operationId = UUID.randomUUID();
		return switch (movement) {
			case AccountMovementResult.Accepted accepted -> {
				BankOperation operation = BankOperation.success(
						operationId,
						request.reference(),
						account.getId(),
						type,
						request.amount(),
						accepted.balanceAfter(),
						Timestamps.now(clock)
				);
				operations.save(operation);
				accounts.save(account);
				yield OperationResults.from(operation);
			}
			case AccountMovementResult.Rejected rejected -> {
				BankOperation operation = BankOperation.rejected(
						operationId,
						request.reference(),
						account.getId(),
						type,
						request.amount(),
						rejected.reason(),
						Timestamps.now(clock)
				);
				operations.save(operation);
				yield OperationResults.from(operation);
			}
		};
	}

}
