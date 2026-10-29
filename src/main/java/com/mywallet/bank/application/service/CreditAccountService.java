package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.port.in.CreditAccountUseCase;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.OperationType;
import org.springframework.stereotype.Service;

@Service
public class CreditAccountService implements CreditAccountUseCase {

	private final OperationApplicationService operations;

	public CreditAccountService(OperationApplicationService operations) {
		this.operations = operations;
	}

	@Override
	public OperationResult execute(OperationCommand command) {
		return operations.execute(command, OperationType.CREDIT);
	}

}
