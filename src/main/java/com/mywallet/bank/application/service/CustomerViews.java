package com.mywallet.bank.application.service;

import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.domain.model.Customer;

final class CustomerViews {

	private CustomerViews() {
	}

	static CustomerView from(Customer customer) {
		return new CustomerView(
				customer.getId(),
				customer.getFirstName(),
				customer.getLastName(),
				customer.getPhone(),
				customer.getEmail(),
				customer.getStatus().name(),
				customer.getCreatedAt()
		);
	}

}
