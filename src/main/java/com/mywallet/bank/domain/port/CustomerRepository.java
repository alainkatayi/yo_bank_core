package com.mywallet.bank.domain.port;

import com.mywallet.bank.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

	Optional<Customer> findById(UUID id);

	Optional<Customer> findByPhone(String phone);

	Customer save(Customer customer);

	List<Customer> search(String term, int page, int size);

	long countSearch(String term);

}
