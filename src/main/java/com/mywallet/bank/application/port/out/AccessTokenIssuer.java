package com.mywallet.bank.application.port.out;

import com.mywallet.bank.application.result.IssuedToken;
import com.mywallet.bank.domain.model.BankAgent;

public interface AccessTokenIssuer {

	IssuedToken issue(BankAgent agent);

}
