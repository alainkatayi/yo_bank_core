package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.Customer;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

	private final SpringDataCustomerRepository customers;
	private final CustomerMapper mapper = new CustomerMapper();

	public CustomerRepositoryAdapter(SpringDataCustomerRepository customers) {
		this.customers = customers;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Customer> findById(UUID id) {
		return customers.findById(id).map(mapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Customer> findByPhone(String phone) {
		return customers.findByPhone(phone).map(mapper::toDomain);
	}

	@Override
	@Transactional
	public Customer save(Customer customer) {
		CustomerJpaEntity existing = customers.findById(customer.getId()).orElse(null);
		if (existing == null) {
			return mapper.toDomain(customers.saveAndFlush(mapper.toNewEntity(customer)));
		}
		mapper.update(existing, customer);
		customers.flush();
		return mapper.toDomain(existing);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Customer> search(String term, int page, int size) {
		return customers.search(term, PageRequest.of(page, size)).stream().map(mapper::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public long countSearch(String term) {
		return customers.countSearch(term);
	}

}
