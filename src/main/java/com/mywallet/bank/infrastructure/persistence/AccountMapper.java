package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Money;

final class AccountMapper {

	BankAccount toDomain(AccountJpaEntity entity) {
		if (entity.getVersion() == null) {
			throw new IllegalStateException("La version du compte persisté est absente");
		}
		String accountNumber = entity.getAccountNumber() == null ? null : entity.getAccountNumber().trim();
		return new BankAccount(
				entity.getId(),
				new AccountNumber(accountNumber),
				entity.getCustomerId(),
				Money.of(entity.getBalance()),
				entity.getStatus(),
				entity.getVersion(),
				entity.getCreatedAt()
		);
	}

	AccountJpaEntity toNewEntity(BankAccount account) {
		return new AccountJpaEntity(
				account.getId(),
				account.getAccountNumber().value(),
				account.getCustomerId(),
				account.getBalance().amount(),
				account.getStatus(),
				account.getCreatedAt()
		);
	}

	void update(AccountJpaEntity entity, BankAccount account) {
		entity.apply(account.getBalance().amount(), account.getStatus());
	}

}
