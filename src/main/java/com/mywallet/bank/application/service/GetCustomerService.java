package com.mywallet.bank.application.service;

import com.mywallet.bank.application.exception.CustomerNotFoundException;
import com.mywallet.bank.application.port.in.GetCustomerUseCase;
import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetCustomerService implements GetCustomerUseCase {

	private final CustomerRepository customers;

	public GetCustomerService(CustomerRepository customers) {
		this.customers = customers;
	}

	@Override
	public CustomerView execute(UUID customerId) {
		if (customerId == null) {
			throw new IllegalArgumentException("L'identifiant client est obligatoire");
		}
		return customers.findById(customerId)
				.map(CustomerViews::from)
				.orElseThrow(CustomerNotFoundException::new);
	}

}
