package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.AdminAuditCommand;
import com.mywallet.bank.application.command.AdminActor;
import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.AuditTargetType;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.port.AccountRepository;
import com.mywallet.bank.domain.port.AuditLogRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class AccountOpeningExecutor {

	private final AccountRepository accounts;
	private final AuditLogRepository auditLog;
	private final Clock clock;

	public AccountOpeningExecutor(AccountRepository accounts, AuditLogRepository auditLog, Clock clock) {
		this.accounts = accounts;
		this.auditLog = auditLog;
		this.clock = clock;
	}

	@Transactional
	public BankAccount open(UUID customerId, AccountNumber accountNumber, AdminActor actor) {
		if (accounts.findByAccountNumber(accountNumber).isPresent()) {
			throw new AccountNumberTakenException();
		}
		BankAccount account = new BankAccount(
				UUID.randomUUID(),
				accountNumber,
				customerId,
				Money.zero(),
				AccountStatus.ACTIVE,
				0L,
				Timestamps.now(clock)
		);
		BankAccount saved;
		try {
			saved = accounts.save(account);
		} catch (DataIntegrityViolationException exception) {
			if (UniqueConstraints.matches(exception, "account_number")) {
				throw new AccountNumberTakenException();
			}
			throw exception;
		}
		Audits.append(auditLog, new AdminAuditCommand(
				actor,
				AuditAction.ACCOUNT_OPENED,
				AuditTargetType.ACCOUNT,
				saved.getAccountNumber().value(),
				"customerId=" + customerId
		), clock);
		return saved;
	}

}
