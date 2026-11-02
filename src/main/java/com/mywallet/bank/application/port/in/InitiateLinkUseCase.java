package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.result.InitiateLinkResult;

public interface InitiateLinkUseCase {

	InitiateLinkResult execute(String accountNumber, String phone);

}
