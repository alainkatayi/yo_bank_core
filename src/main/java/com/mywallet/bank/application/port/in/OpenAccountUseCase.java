package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.result.AccountView;

import java.util.UUID;

public interface OpenAccountUseCase {

	AccountView execute(UUID customerId);

}
