package com.mywallet.bank.application.service;

import com.mywallet.bank.application.PageQuery;
import com.mywallet.bank.application.PageResult;
import com.mywallet.bank.application.port.in.SearchCustomersUseCase;
import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class SearchCustomersService implements SearchCustomersUseCase {

	private final CustomerRepository customers;

	public SearchCustomersService(CustomerRepository customers) {
		this.customers = customers;
	}

	@Override
	public PageResult<CustomerView> execute(String search, PageQuery page) {
		String term = normalize(search);
		return new PageResult<>(
				customers.search(term, page.page(), page.size()).stream().map(CustomerViews::from).toList(),
				page.page(),
				page.size(),
				customers.countSearch(term)
		);
	}

	static String normalize(String search) {
		if (search == null || search.isBlank()) {
			return "";
		}
		return search.trim().toLowerCase(Locale.ROOT);
	}

}
