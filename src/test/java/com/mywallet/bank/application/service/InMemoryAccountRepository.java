package com.mywallet.bank.application.service;

import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.port.AccountRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class InMemoryAccountRepository implements AccountRepository {

	private final Map<UUID, BankAccount> accounts = new HashMap<>();
	private int saveCount;

	void store(BankAccount account) {
		accounts.put(account.getId(), account);
	}

	int saveCount() {
		return saveCount;
	}

	BankAccount only() {
		return accounts.values().iterator().next();
	}

	@Override
	public Optional<BankAccount> findByAccountNumber(AccountNumber accountNumber) {
		return accounts.values().stream()
				.filter(account -> account.getAccountNumber().equals(accountNumber))
				.findFirst();
	}

	@Override
	public Optional<BankAccount> findById(UUID id) {
		return Optional.ofNullable(accounts.get(id));
	}

	@Override
	public BankAccount save(BankAccount account) {
		saveCount++;
		accounts.put(account.getId(), account);
		return account;
	}

}
