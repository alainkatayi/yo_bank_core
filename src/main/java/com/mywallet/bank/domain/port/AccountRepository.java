package com.mywallet.bank.domain.port;

import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.BankAccount;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

	Optional<BankAccount> findByAccountNumber(AccountNumber accountNumber);

	Optional<BankAccount> findById(UUID id);

	BankAccount save(BankAccount account);

}
