package com.mywallet.bank.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {

	Optional<CustomerJpaEntity> findByPhone(String phone);

	@Query("""
			select c from CustomerJpaEntity c
			where :term = ''
			   or locate(:term, lower(c.firstName)) > 0
			   or locate(:term, lower(c.lastName)) > 0
			   or locate(:term, lower(c.phone)) > 0
			order by c.createdAt desc
			""")
	List<CustomerJpaEntity> search(@Param("term") String term, Pageable pageable);

	@Query("""
			select count(c) from CustomerJpaEntity c
			where :term = ''
			   or locate(:term, lower(c.firstName)) > 0
			   or locate(:term, lower(c.lastName)) > 0
			   or locate(:term, lower(c.phone)) > 0
			""")
	long countSearch(@Param("term") String term);

}
