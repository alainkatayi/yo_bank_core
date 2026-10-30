package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.CustomerStatusCommand;
import com.mywallet.bank.application.port.in.ReactivateCustomerUseCase;
import com.mywallet.bank.application.result.CustomerView;
import org.springframework.stereotype.Service;

@Service
public class ReactivateCustomerService implements ReactivateCustomerUseCase {

	private final CustomerStatusExecutor executor;

	public ReactivateCustomerService(CustomerStatusExecutor executor) {
		this.executor = executor;
	}

	@Override
	public CustomerView execute(CustomerStatusCommand command) {
		return executor.reactivate(command);
	}

}
