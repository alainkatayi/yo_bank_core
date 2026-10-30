package com.mywallet.bank.application.port.in;

public interface VerifyLinkUseCase {

	boolean execute(String accountNumber, String phone);

}
