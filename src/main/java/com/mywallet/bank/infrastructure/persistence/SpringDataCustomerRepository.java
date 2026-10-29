package com.mywallet.bank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {

	Optional<CustomerJpaEntity> findByPhone(String phone);

}
