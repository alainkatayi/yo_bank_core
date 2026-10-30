package com.mywallet.bank.api;

import com.mywallet.bank.application.port.in.VerifyLinkUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank/links")
public class BankLinkController {

	private final VerifyLinkUseCase links;

	public BankLinkController(VerifyLinkUseCase links) {
		this.links = links;
	}

	@PostMapping("/verify")
	public VerifyLinkResponse verify(@Valid @RequestBody VerifyLinkRequest request) {
		return new VerifyLinkResponse(links.execute(request.accountNumber(), request.phone()));
	}

}
