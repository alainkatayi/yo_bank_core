package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.result.OperationResult;

public interface CreditAccountUseCase {

	OperationResult execute(OperationCommand command);

}
