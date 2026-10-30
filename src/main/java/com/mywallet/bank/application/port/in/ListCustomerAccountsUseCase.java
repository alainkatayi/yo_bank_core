package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.result.AdminAccountView;

import java.util.List;
import java.util.UUID;

public interface ListCustomerAccountsUseCase {

	List<AdminAccountView> execute(UUID customerId);

}
