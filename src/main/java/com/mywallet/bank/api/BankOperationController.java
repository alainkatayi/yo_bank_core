package com.mywallet.bank.api;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.port.in.CreditAccountUseCase;
import com.mywallet.bank.application.port.in.DebitAccountUseCase;
import com.mywallet.bank.application.port.in.GetOperationUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank/operations")
public class BankOperationController {

	private final DebitAccountUseCase debits;
	private final CreditAccountUseCase credits;
	private final GetOperationUseCase operations;

	public BankOperationController(
			DebitAccountUseCase debits,
			CreditAccountUseCase credits,
			GetOperationUseCase operations
	) {
		this.debits = debits;
		this.credits = credits;
		this.operations = operations;
	}

	@PostMapping("/debit")
	public OperationResponse debit(@Valid @RequestBody OperationRequest request) {
		return ApiResponses.from(debits.execute(command(request)));
	}

	@PostMapping("/credit")
	public OperationResponse credit(@Valid @RequestBody OperationRequest request) {
		return ApiResponses.from(credits.execute(command(request)));
	}

	@GetMapping("/{reference}")
	public OperationResponse get(@PathVariable String reference) {
		return ApiResponses.from(operations.execute(reference));
	}

	private static OperationCommand command(OperationRequest request) {
		return new OperationCommand(request.reference(), request.accountNumber(), request.amount());
	}

}
