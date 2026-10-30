package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.AccountStatusCommand;
import com.mywallet.bank.application.port.in.BlockAccountUseCase;
import com.mywallet.bank.application.result.AdminAccountView;
import org.springframework.stereotype.Service;

@Service
public class BlockAccountService implements BlockAccountUseCase {

	private final AccountStatusRetries retries;

	public BlockAccountService(AccountStatusRetries retries) {
		this.retries = retries;
	}

	@Override
	public AdminAccountView execute(AccountStatusCommand command) {
		return retries.change(command, AccountStatusChange.BLOCK);
	}

}
