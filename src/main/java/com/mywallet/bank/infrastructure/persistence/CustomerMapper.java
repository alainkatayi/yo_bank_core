package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.Customer;

final class CustomerMapper {

	Customer toDomain(CustomerJpaEntity entity) {
		return new Customer(
				entity.getId(),
				entity.getFirstName(),
				entity.getLastName(),
				entity.getPhone(),
				entity.getEmail(),
				entity.getStatus(),
				entity.getCreatedAt()
		);
	}

	CustomerJpaEntity toNewEntity(Customer customer) {
		return new CustomerJpaEntity(
				customer.getId(),
				customer.getFirstName(),
				customer.getLastName(),
				customer.getPhone(),
				customer.getEmail(),
				customer.getStatus(),
				customer.getCreatedAt()
		);
	}

	void update(CustomerJpaEntity entity, Customer customer) {
		entity.apply(
				customer.getFirstName(),
				customer.getLastName(),
				customer.getPhone(),
				customer.getEmail(),
				customer.getStatus()
		);
	}

}
