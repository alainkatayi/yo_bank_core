package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.AccountStatusCommand;
import com.mywallet.bank.application.port.in.UnblockAccountUseCase;
import com.mywallet.bank.application.result.AdminAccountView;
import org.springframework.stereotype.Service;

@Service
public class UnblockAccountService implements UnblockAccountUseCase {

	private final AccountStatusRetries retries;

	public UnblockAccountService(AccountStatusRetries retries) {
		this.retries = retries;
	}

	@Override
	public AdminAccountView execute(AccountStatusCommand command) {
		return retries.change(command, AccountStatusChange.UNBLOCK);
	}

}
