package com.mywallet.bank.api;

import com.mywallet.bank.application.result.AccountView;
import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.application.result.OperationResult;

final class ApiResponses {

	private ApiResponses() {
	}

	static OperationResponse from(OperationResult result) {
		return new OperationResponse(
				result.reference(),
				result.type().name(),
				result.status().name(),
				result.rejectionReason() == null ? null : result.rejectionReason().name(),
				result.balanceAfter() == null ? null : result.balanceAfter().amount().toPlainString(),
				result.createdAt()
		);
	}

	static AccountResponse from(AccountView account) {
		return new AccountResponse(
				account.accountNumber(),
				account.maskedHolderName(),
				account.balance().amount().toPlainString(),
				account.status().name()
		);
	}

	static CustomerResponse from(CustomerView customer) {
		return new CustomerResponse(
				customer.id(),
				customer.firstName(),
				customer.lastName(),
				customer.phone(),
				customer.email(),
				customer.status(),
				customer.createdAt()
		);
	}

}
