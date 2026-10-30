package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.command.DepositFundsCommand;
import com.mywallet.bank.application.result.OperationResult;

public interface DepositFundsUseCase {

	OperationResult execute(DepositFundsCommand command);

}
