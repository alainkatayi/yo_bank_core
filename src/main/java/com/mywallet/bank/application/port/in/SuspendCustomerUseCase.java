package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.command.CustomerStatusCommand;
import com.mywallet.bank.application.result.CustomerView;

public interface SuspendCustomerUseCase {

	CustomerView execute(CustomerStatusCommand command);

}
