package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.command.LoginCommand;
import com.mywallet.bank.application.result.LoginResult;

public interface LoginAgentUseCase {

	LoginResult execute(LoginCommand command);

}
