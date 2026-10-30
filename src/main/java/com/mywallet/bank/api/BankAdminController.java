package com.mywallet.bank.api;

import com.mywallet.bank.application.command.AccountStatusCommand;
import com.mywallet.bank.application.command.CreateCustomerCommand;
import com.mywallet.bank.application.command.CustomerStatusCommand;
import com.mywallet.bank.application.command.DepositFundsCommand;
import com.mywallet.bank.application.command.OpenAccountCommand;
import com.mywallet.bank.application.port.in.BlockAccountUseCase;
import com.mywallet.bank.application.port.in.CloseAccountUseCase;
import com.mywallet.bank.application.port.in.CreateCustomerUseCase;
import com.mywallet.bank.application.port.in.DepositFundsUseCase;
import com.mywallet.bank.application.port.in.OpenAccountUseCase;
import com.mywallet.bank.application.port.in.ReactivateCustomerUseCase;
import com.mywallet.bank.application.port.in.SuspendCustomerUseCase;
import com.mywallet.bank.application.port.in.UnblockAccountUseCase;
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
	private final BlockAccountUseCase blocks;
	private final UnblockAccountUseCase unblocks;
	private final CloseAccountUseCase closures;
	private final SuspendCustomerUseCase suspensions;
	private final ReactivateCustomerUseCase reactivations;

	public BankAdminController(
			CreateCustomerUseCase customers,
			OpenAccountUseCase accounts,
			DepositFundsUseCase deposits,
			BlockAccountUseCase blocks,
			UnblockAccountUseCase unblocks,
			CloseAccountUseCase closures,
			SuspendCustomerUseCase suspensions,
			ReactivateCustomerUseCase reactivations
	) {
		this.customers = customers;
		this.accounts = accounts;
		this.deposits = deposits;
		this.blocks = blocks;
		this.unblocks = unblocks;
		this.closures = closures;
		this.suspensions = suspensions;
		this.reactivations = reactivations;
	}

	@PostMapping("/customers")
	public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
		CustomerView customer = customers.execute(new CreateCustomerCommand(
				CurrentAdmin.get(),
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
		AccountView account = accounts.execute(new OpenAccountCommand(CurrentAdmin.get(), id));
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/bank/accounts/{number}")
				.buildAndExpand(account.accountNumber())
				.toUri();
		return ResponseEntity.created(location).body(ApiResponses.from(account));
	}

	@PostMapping("/accounts/{number}/deposit")
	public OperationResponse deposit(@PathVariable String number, @Valid @RequestBody DepositRequest request) {
		return ApiResponses.from(deposits.execute(new DepositFundsCommand(CurrentAdmin.get(), number, request.amount())));
	}

	@PostMapping("/accounts/{number}/block")
	public AdminAccountResponse block(@PathVariable String number) {
		return ApiResponses.from(blocks.execute(new AccountStatusCommand(CurrentAdmin.get(), number)));
	}

	@PostMapping("/accounts/{number}/unblock")
	public AdminAccountResponse unblock(@PathVariable String number) {
		return ApiResponses.from(unblocks.execute(new AccountStatusCommand(CurrentAdmin.get(), number)));
	}

	@PostMapping("/accounts/{number}/close")
	public AdminAccountResponse close(@PathVariable String number) {
		return ApiResponses.from(closures.execute(new AccountStatusCommand(CurrentAdmin.get(), number)));
	}

	@PostMapping("/customers/{id}/suspend")
	public CustomerResponse suspend(@PathVariable UUID id) {
		return ApiResponses.from(suspensions.execute(new CustomerStatusCommand(CurrentAdmin.get(), id)));
	}

	@PostMapping("/customers/{id}/reactivate")
	public CustomerResponse reactivate(@PathVariable UUID id) {
		return ApiResponses.from(reactivations.execute(new CustomerStatusCommand(CurrentAdmin.get(), id)));
	}

}
