package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.CustomerStatusCommand;
import com.mywallet.bank.application.port.in.SuspendCustomerUseCase;
import com.mywallet.bank.application.result.CustomerView;
import org.springframework.stereotype.Service;

@Service
public class SuspendCustomerService implements SuspendCustomerUseCase {

	private final CustomerStatusExecutor executor;

	public SuspendCustomerService(CustomerStatusExecutor executor) {
		this.executor = executor;
	}

	@Override
	public CustomerView execute(CustomerStatusCommand command) {
		return executor.suspend(command);
	}

}
