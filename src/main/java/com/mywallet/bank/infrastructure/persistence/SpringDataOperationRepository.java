package com.mywallet.bank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataOperationRepository extends JpaRepository<OperationJpaEntity, UUID> {

	Optional<OperationJpaEntity> findByReference(String reference);

}
