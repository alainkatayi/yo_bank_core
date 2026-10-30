package com.mywallet.bank.api;

import com.mywallet.bank.application.port.in.GetAccountUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank/accounts")
public class BankAccountController {

	private final GetAccountUseCase accounts;

	public BankAccountController(GetAccountUseCase accounts) {
		this.accounts = accounts;
	}

	@GetMapping("/{number}")
	public AccountResponse get(@PathVariable String number) {
		return ApiResponses.from(accounts.execute(number));
	}

}
