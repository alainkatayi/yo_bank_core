package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.command.CreateCustomerCommand;
import com.mywallet.bank.application.result.CustomerView;

public interface CreateCustomerUseCase {

	CustomerView execute(CreateCustomerCommand command);

}
