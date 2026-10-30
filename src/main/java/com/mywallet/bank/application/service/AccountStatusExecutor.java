package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.AccountStatusCommand;
import com.mywallet.bank.application.command.AdminAuditCommand;
import com.mywallet.bank.application.exception.AccountNotFoundException;
import com.mywallet.bank.application.result.AdminAccountView;
import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.AuditTargetType;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.port.AccountRepository;
import com.mywallet.bank.domain.port.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AccountStatusExecutor {

	private final AccountRepository accounts;
	private final AuditLogRepository auditLog;
	private final Clock clock;

	public AccountStatusExecutor(AccountRepository accounts, AuditLogRepository auditLog, Clock clock) {
		this.accounts = accounts;
		this.auditLog = auditLog;
		this.clock = clock;
	}

	/**
	 * Une seule tentative. Le retry reste dans {@link AccountAdministrationService},
	 * hors de cette transaction.
	 */
	@Transactional
	public AdminAccountView apply(AccountStatusCommand command, AccountStatusChange change) {
		AccountNumber number = new AccountNumber(command.accountNumber());
		BankAccount account = accounts.findByAccountNumber(number)
				.orElseThrow(() -> new AccountNotFoundException(number.value()));
		AuditAction action = switch (change) {
			case BLOCK -> {
				account.block();
				yield AuditAction.ACCOUNT_BLOCKED;
			}
			case UNBLOCK -> {
				account.unblock();
				yield AuditAction.ACCOUNT_UNBLOCKED;
			}
			case CLOSE -> {
				account.close();
				yield AuditAction.ACCOUNT_CLOSED;
			}
		};
		BankAccount saved = accounts.save(account);
		Audits.append(auditLog, new AdminAuditCommand(
				command.actor(),
				action,
				AuditTargetType.ACCOUNT,
				saved.getAccountNumber().value(),
				null
		), clock);
		return new AdminAccountView(saved.getAccountNumber().value(), saved.getBalance(), saved.getStatus());
	}

}
