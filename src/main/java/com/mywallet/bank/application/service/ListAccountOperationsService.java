package com.mywallet.bank.application.service;

import com.mywallet.bank.application.PageQuery;
import com.mywallet.bank.application.PageResult;
import com.mywallet.bank.application.exception.AccountNotFoundException;
import com.mywallet.bank.application.port.in.ListAccountOperationsUseCase;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.port.AccountRepository;
import com.mywallet.bank.domain.port.OperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListAccountOperationsService implements ListAccountOperationsUseCase {

	private final AccountRepository accounts;
	private final OperationRepository operations;

	public ListAccountOperationsService(AccountRepository accounts, OperationRepository operations) {
		this.accounts = accounts;
		this.operations = operations;
	}

	@Override
	public PageResult<OperationResult> execute(
			String accountNumber,
			OperationType type,
			OperationStatus status,
			PageQuery page
	) {
		AccountNumber number = new AccountNumber(accountNumber);
		BankAccount account = accounts.findByAccountNumber(number)
				.orElseThrow(() -> new AccountNotFoundException(number.value()));
		return new PageResult<>(
				operations.search(account.getId(), type, status, page.page(), page.size()).stream()
						.map(OperationResults::from)
						.toList(),
				page.page(),
				page.size(),
				operations.countSearch(account.getId(), type, status)
		);
	}

}
