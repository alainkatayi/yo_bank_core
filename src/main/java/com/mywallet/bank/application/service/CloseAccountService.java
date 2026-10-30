package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.AccountStatusCommand;
import com.mywallet.bank.application.port.in.CloseAccountUseCase;
import com.mywallet.bank.application.result.AdminAccountView;
import org.springframework.stereotype.Service;

@Service
public class CloseAccountService implements CloseAccountUseCase {

	private final AccountStatusRetries retries;

	public CloseAccountService(AccountStatusRetries retries) {
		this.retries = retries;
	}

	@Override
	public AdminAccountView execute(AccountStatusCommand command) {
		return retries.change(command, AccountStatusChange.CLOSE);
	}

}
