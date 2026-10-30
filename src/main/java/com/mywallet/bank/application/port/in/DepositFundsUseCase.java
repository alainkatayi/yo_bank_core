package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.result.OperationResult;

public interface DepositFundsUseCase {

	OperationResult execute(String accountNumber, String amount);

}
