package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.result.AccountView;

public interface GetAccountUseCase {

	AccountView execute(String accountNumber);

}
