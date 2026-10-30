package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.AdminAuditCommand;
import com.mywallet.bank.application.command.DepositFundsCommand;
import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.port.in.DepositFundsUseCase;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.AuditTargetType;
import com.mywallet.bank.domain.model.OperationType;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Ne jamais appeler ce bean depuis une transaction déjà ouverte :
 * le crédit réessaie hors transaction, et l'audit n'est écrit
 * que dans la transaction de la tentative réussie.
 */
@Service
public class DepositFundsService implements DepositFundsUseCase {

	static final String REFERENCE_PREFIX = "ADMIN-DEP-";

	private final OperationApplicationService operations;

	public DepositFundsService(OperationApplicationService operations) {
		this.operations = operations;
	}

	@Override
	public OperationResult execute(DepositFundsCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("La commande est obligatoire");
		}
		String reference = REFERENCE_PREFIX + UUID.randomUUID();
		OperationCommand operation = new OperationCommand(reference, command.accountNumber(), command.amount());
		AdminAuditCommand audit = new AdminAuditCommand(
				command.actor(),
				AuditAction.ACCOUNT_DEPOSIT,
				AuditTargetType.ACCOUNT,
				command.accountNumber(),
				"amount=" + command.amount() + ";reference=" + reference
		);
		return operations.execute(operation, OperationType.CREDIT, audit);
	}

}
