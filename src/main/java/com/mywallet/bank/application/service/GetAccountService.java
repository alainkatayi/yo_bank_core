package com.mywallet.bank.application.service;

import com.mywallet.bank.application.exception.AccountNotFoundException;
import com.mywallet.bank.application.port.in.GetAccountUseCase;
import com.mywallet.bank.application.result.AccountView;
import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Customer;
import com.mywallet.bank.domain.port.AccountRepository;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetAccountService implements GetAccountUseCase {

	private final AccountRepository accounts;
	private final CustomerRepository customers;

	public GetAccountService(AccountRepository accounts, CustomerRepository customers) {
		this.accounts = accounts;
		this.customers = customers;
	}

	@Override
	public AccountView execute(String accountNumber) {
		AccountNumber number = new AccountNumber(accountNumber);
		BankAccount account = accounts.findByAccountNumber(number)
				.orElseThrow(() -> new AccountNotFoundException(number.value()));
		Customer customer = customers.findById(account.getCustomerId())
				.orElseThrow(() -> new IllegalStateException(
						"Titulaire introuvable pour le compte " + number.value()
				));
		return AccountViews.from(account, customer);
	}

}
