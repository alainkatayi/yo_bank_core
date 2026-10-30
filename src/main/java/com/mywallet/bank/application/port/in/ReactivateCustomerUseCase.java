package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.command.CustomerStatusCommand;
import com.mywallet.bank.application.result.CustomerView;

public interface ReactivateCustomerUseCase {

	CustomerView execute(CustomerStatusCommand command);

}
