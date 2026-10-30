package com.mywallet.bank.application.service;

import com.mywallet.bank.application.port.in.VerifyLinkUseCase;
import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.port.AccountRepository;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VerifyLinkService implements VerifyLinkUseCase {

	private final AccountRepository accounts;
	private final CustomerRepository customers;

	public VerifyLinkService(AccountRepository accounts, CustomerRepository customers) {
		this.accounts = accounts;
		this.customers = customers;
	}

	@Override
	public boolean execute(String accountNumber, String phone) {
		if (phone == null || phone.isBlank()) {
			return false;
		}
		return accounts.findByAccountNumber(new AccountNumber(accountNumber))
				.map(BankAccount::getCustomerId)
				.flatMap(customers::findById)
				.map(customer -> phone.equals(customer.getPhone()))
				.orElse(false);
	}

}
