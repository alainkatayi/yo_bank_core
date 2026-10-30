package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.port.in.CreditAccountUseCase;
import com.mywallet.bank.application.port.in.DepositFundsUseCase;
import com.mywallet.bank.application.result.OperationResult;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Ne jamais appeler ce bean depuis une transaction déjà ouverte :
 * {@link CreditAccountUseCase} réessaie hors transaction.
 */
@Service
public class DepositFundsService implements DepositFundsUseCase {

	static final String REFERENCE_PREFIX = "ADMIN-DEP-";

	private final CreditAccountUseCase credits;

	public DepositFundsService(CreditAccountUseCase credits) {
		this.credits = credits;
	}

	@Override
	public OperationResult execute(String accountNumber, String amount) {
		String reference = REFERENCE_PREFIX + UUID.randomUUID();
		return credits.execute(new OperationCommand(reference, accountNumber, amount));
	}

}
