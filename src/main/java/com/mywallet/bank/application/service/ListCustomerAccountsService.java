package com.mywallet.bank.application.service;

import com.mywallet.bank.application.exception.CustomerNotFoundException;
import com.mywallet.bank.application.port.in.ListCustomerAccountsUseCase;
import com.mywallet.bank.application.result.AdminAccountView;
import com.mywallet.bank.domain.port.AccountRepository;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListCustomerAccountsService implements ListCustomerAccountsUseCase {

	private final CustomerRepository customers;
	private final AccountRepository accounts;

	public ListCustomerAccountsService(CustomerRepository customers, AccountRepository accounts) {
		this.customers = customers;
		this.accounts = accounts;
	}

	@Override
	public List<AdminAccountView> execute(UUID customerId) {
		if (customerId == null) {
			throw new IllegalArgumentException("L'identifiant client est obligatoire");
		}
		if (customers.findById(customerId).isEmpty()) {
			throw new CustomerNotFoundException();
		}
		return accounts.findByCustomerId(customerId).stream()
				.map(account -> new AdminAccountView(
						account.getAccountNumber().value(),
						account.getBalance(),
						account.getStatus()
				))
				.toList();
	}

}
