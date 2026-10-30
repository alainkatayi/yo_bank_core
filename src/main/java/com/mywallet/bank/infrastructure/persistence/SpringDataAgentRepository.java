package com.mywallet.bank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataAgentRepository extends JpaRepository<BankAgentJpaEntity, UUID> {

	Optional<BankAgentJpaEntity> findByUsername(String username);

	boolean existsByUsername(String username);

	List<BankAgentJpaEntity> findAllByOrderByUsernameAsc();

}
