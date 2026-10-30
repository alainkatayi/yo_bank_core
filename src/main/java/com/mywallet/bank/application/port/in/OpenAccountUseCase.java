package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.command.OpenAccountCommand;
import com.mywallet.bank.application.result.AccountView;

public interface OpenAccountUseCase {

	AccountView execute(OpenAccountCommand command);

}
