package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.CreateCustomerCommand;
import com.mywallet.bank.application.exception.DuplicatePhoneException;
import com.mywallet.bank.application.port.in.CreateCustomerUseCase;
import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class CreateCustomerService implements CreateCustomerUseCase {

	private final CustomerRepository customers;
	private final CreateCustomerExecutor executor;

	public CreateCustomerService(CustomerRepository customers, CreateCustomerExecutor executor) {
		this.customers = customers;
		this.executor = executor;
	}

	@Override
	public CustomerView execute(CreateCustomerCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("La commande est obligatoire");
		}
		if (customers.findByPhone(command.phone()).isPresent()) {
			throw new DuplicatePhoneException();
		}
		try {
			return executor.create(command);
		} catch (DataIntegrityViolationException exception) {
			if (UniqueConstraints.matches(exception, "phone")) {
				throw new DuplicatePhoneException();
			}
			throw exception;
		}
	}

}
