package com.mywallet.bank.api;

import com.mywallet.bank.application.PageQuery;
import com.mywallet.bank.application.PageResult;
import com.mywallet.bank.application.port.in.GetCustomerUseCase;
import com.mywallet.bank.application.port.in.ListAccountOperationsUseCase;
import com.mywallet.bank.application.port.in.ListCustomerAccountsUseCase;
import com.mywallet.bank.application.port.in.SearchAuditLogUseCase;
import com.mywallet.bank.application.port.in.SearchCustomersUseCase;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bank/admin")
public class BankAdminQueryController {

	private final SearchCustomersUseCase customers;
	private final GetCustomerUseCase customer;
	private final ListCustomerAccountsUseCase accounts;
	private final ListAccountOperationsUseCase operations;
	private final SearchAuditLogUseCase audit;

	public BankAdminQueryController(
			SearchCustomersUseCase customers,
			GetCustomerUseCase customer,
			ListCustomerAccountsUseCase accounts,
			ListAccountOperationsUseCase operations,
			SearchAuditLogUseCase audit
	) {
		this.customers = customers;
		this.customer = customer;
		this.accounts = accounts;
		this.operations = operations;
		this.audit = audit;
	}

	@GetMapping("/customers")
	public PageResponse<CustomerResponse> searchCustomers(
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		return page(customers.execute(search, new PageQuery(page, size)), ApiResponses::from);
	}

	@GetMapping("/customers/{id}")
	public CustomerResponse getCustomer(@PathVariable UUID id) {
		return ApiResponses.from(customer.execute(id));
	}

	@GetMapping("/customers/{id}/accounts")
	public List<AdminAccountResponse> customerAccounts(@PathVariable UUID id) {
		return accounts.execute(id).stream().map(ApiResponses::from).toList();
	}

	@GetMapping("/accounts/{number}/operations")
	public PageResponse<OperationResponse> operations(
			@PathVariable String number,
			@RequestParam(required = false) OperationType type,
			@RequestParam(required = false) OperationStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		return page(operations.execute(number, type, status, new PageQuery(page, size)), ApiResponses::from);
	}

	@GetMapping("/audit")
	public PageResponse<AuditEntryResponse> audit(
			@RequestParam(required = false) String actor,
			@RequestParam(required = false) AuditAction action,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		return page(audit.execute(actor, action, new PageQuery(page, size)), ApiResponses::from);
	}

	private static <S, T> PageResponse<T> page(PageResult<S> result, java.util.function.Function<S, T> mapper) {
		return new PageResponse<>(
				result.items().stream().map(mapper).toList(),
				result.page(),
				result.size(),
				result.totalItems()
		);
	}

}
