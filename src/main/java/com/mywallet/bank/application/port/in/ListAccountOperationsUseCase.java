package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.PageQuery;
import com.mywallet.bank.application.PageResult;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;

public interface ListAccountOperationsUseCase {

	PageResult<OperationResult> execute(String accountNumber, OperationType type, OperationStatus status, PageQuery page);

}
