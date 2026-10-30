package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.CreateCustomerCommand;
import com.mywallet.bank.application.exception.DuplicatePhoneException;
import com.mywallet.bank.application.port.in.CreateCustomerUseCase;
import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.domain.model.Customer;
import com.mywallet.bank.domain.model.CustomerStatus;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.UUID;

@Service
public class CreateCustomerService implements CreateCustomerUseCase {

	private final CustomerRepository customers;
	private final Clock clock;

	public CreateCustomerService(CustomerRepository customers, Clock clock) {
		this.customers = customers;
		this.clock = clock;
	}

	@Override
	public CustomerView execute(CreateCustomerCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("La commande est obligatoire");
		}
		if (customers.findByPhone(command.phone()).isPresent()) {
			throw new DuplicatePhoneException();
		}
		Customer customer = new Customer(
				UUID.randomUUID(),
				command.firstName(),
				command.lastName(),
				command.phone(),
				blankToNull(command.email()),
				CustomerStatus.ACTIVE,
				Timestamps.now(clock)
		);
		try {
			return CustomerViews.from(customers.save(customer));
		} catch (DataIntegrityViolationException exception) {
			if (UniqueConstraints.matches(exception, "phone")) {
				throw new DuplicatePhoneException();
			}
			throw exception;
		}
	}

	private static String blankToNull(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		return email;
	}

}
