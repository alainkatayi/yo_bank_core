package com.mywallet.bank.application.service;

import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.BankOperation;

final class OperationResults {

	private OperationResults() {
	}

	static OperationResult from(BankOperation operation) {
		return new OperationResult(
				operation.getReference(),
				operation.getType(),
				operation.getStatus(),
				operation.getRejectionReason(),
				operation.getBalanceAfter(),
				operation.getCreatedAt()
		);
	}

}
