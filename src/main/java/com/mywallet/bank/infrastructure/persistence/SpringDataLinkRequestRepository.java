package com.mywallet.bank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataLinkRequestRepository extends JpaRepository<LinkRequestJpaEntity, UUID> {
}
