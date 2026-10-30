package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.command.AccountStatusCommand;
import com.mywallet.bank.application.result.AdminAccountView;

public interface CloseAccountUseCase {

	AdminAccountView execute(AccountStatusCommand command);

}
