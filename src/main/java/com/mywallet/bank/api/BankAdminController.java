package com.mywallet.bank.api;

import com.mywallet.bank.application.command.CreateCustomerCommand;
import com.mywallet.bank.application.port.in.CreateCustomerUseCase;
import com.mywallet.bank.application.port.in.DepositFundsUseCase;
import com.mywallet.bank.application.port.in.OpenAccountUseCase;
import com.mywallet.bank.application.result.AccountView;
import com.mywallet.bank.application.result.CustomerView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/bank/admin")
public class BankAdminController {

	private final CreateCustomerUseCase customers;
	private final OpenAccountUseCase accounts;
	private final DepositFundsUseCase deposits;

	public BankAdminController(
			CreateCustomerUseCase customers,
			OpenAccountUseCase accounts,
			DepositFundsUseCase deposits
	) {
		this.customers = customers;
		this.accounts = accounts;
		this.deposits = deposits;
	}

	@PostMapping("/customers")
	public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
		CustomerView customer = customers.execute(new CreateCustomerCommand(
				request.firstName(),
				request.lastName(),
				request.phone(),
				request.email()
		));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(customer.id())
				.toUri();
		return ResponseEntity.created(location).body(ApiResponses.from(customer));
	}

	@PostMapping("/customers/{id}/accounts")
	public ResponseEntity<AccountResponse> openAccount(@PathVariable UUID id) {
		AccountView account = accounts.execute(id);
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/bank/accounts/{number}")
				.buildAndExpand(account.accountNumber())
				.toUri();
		return ResponseEntity.created(location).body(ApiResponses.from(account));
	}

	@PostMapping("/accounts/{number}/deposit")
	public OperationResponse deposit(@PathVariable String number, @Valid @RequestBody DepositRequest request) {
		return ApiResponses.from(deposits.execute(number, request.amount()));
	}

}
