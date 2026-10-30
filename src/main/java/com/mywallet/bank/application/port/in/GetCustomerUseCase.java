package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.result.CustomerView;

import java.util.UUID;

public interface GetCustomerUseCase {

	CustomerView execute(UUID customerId);

}
