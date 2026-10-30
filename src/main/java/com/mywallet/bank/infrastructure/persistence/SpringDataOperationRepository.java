package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataOperationRepository extends JpaRepository<OperationJpaEntity, UUID> {

	Optional<OperationJpaEntity> findByReference(String reference);

	@Query("""
			select o from OperationJpaEntity o
			where o.accountId = :accountId
			  and (:filterType = false or o.type = :type)
			  and (:filterStatus = false or o.status = :status)
			order by o.createdAt desc
			""")
	List<OperationJpaEntity> search(
			@Param("accountId") UUID accountId,
			@Param("filterType") boolean filterType,
			@Param("type") OperationType type,
			@Param("filterStatus") boolean filterStatus,
			@Param("status") OperationStatus status,
			Pageable pageable
	);

	@Query("""
			select count(o) from OperationJpaEntity o
			where o.accountId = :accountId
			  and (:filterType = false or o.type = :type)
			  and (:filterStatus = false or o.status = :status)
			""")
	long countSearch(
			@Param("accountId") UUID accountId,
			@Param("filterType") boolean filterType,
			@Param("type") OperationType type,
			@Param("filterStatus") boolean filterStatus,
			@Param("status") OperationStatus status
	);

}
