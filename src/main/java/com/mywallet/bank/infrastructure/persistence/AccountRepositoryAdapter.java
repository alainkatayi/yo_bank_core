package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.port.AccountRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {

	private final SpringDataAccountRepository accounts;
	private final AccountMapper mapper = new AccountMapper();

	public AccountRepositoryAdapter(SpringDataAccountRepository accounts) {
		this.accounts = accounts;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<BankAccount> findByAccountNumber(AccountNumber accountNumber) {
		return accounts.findByAccountNumber(accountNumber.value()).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<BankAccount> findById(UUID id) {
		return accounts.findById(id).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<BankAccount> findByCustomerId(UUID customerId) {
		return accounts.findByCustomerIdOrderByCreatedAtAsc(customerId).stream().map(mapper::toDomain).toList();
	}

	@Override
	@Transactional
	public BankAccount save(BankAccount account) {
		AccountJpaEntity existing = accounts.findById(account.getId()).orElse(null);
		if (existing == null) {
			if (account.getVersion() != 0L) {
				throw new IllegalArgumentException("Un nouveau compte doit être enregistré à la version 0");
			}
			return mapper.toDomain(accounts.saveAndFlush(mapper.toNewEntity(account)));
		}
		if (!Long.valueOf(account.getVersion()).equals(existing.getVersion())) {
			throw new ObjectOptimisticLockingFailureException(AccountJpaEntity.class, account.getId());
		}
		mapper.update(existing, account);
		accounts.flush();
		return mapper.toDomain(existing);
	}

}
