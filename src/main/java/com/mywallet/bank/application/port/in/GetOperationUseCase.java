package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.result.OperationResult;

public interface GetOperationUseCase {

	OperationResult execute(String reference);

}
