package com.mywallet.bank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {

	Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);

}
