package com.mywallet.bank.api;

import com.mywallet.bank.application.exception.InvalidLinkCodeException;
import com.mywallet.bank.application.port.in.ConfirmLinkUseCase;
import com.mywallet.bank.application.port.in.InitiateLinkUseCase;
import com.mywallet.bank.application.result.ConfirmLinkResult;
import com.mywallet.bank.application.result.InitiateLinkResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/bank/links")
public class BankLinkController {

	private final InitiateLinkUseCase initiations;
	private final ConfirmLinkUseCase confirmations;

	public BankLinkController(InitiateLinkUseCase initiations, ConfirmLinkUseCase confirmations) {
		this.initiations = initiations;
		this.confirmations = confirmations;
	}

	@PostMapping("/initiate")
	public ResponseEntity<InitiateLinkResponse> initiate(@Valid @RequestBody InitiateLinkRequest request) {
		InitiateLinkResult result = initiations.execute(request.accountNumber(), request.phone());
		return ResponseEntity.accepted().body(new InitiateLinkResponse(result.linkId(), result.expiresIn()));
	}

	@PostMapping("/confirm")
	public ConfirmLinkResponse confirm(@RequestBody ConfirmLinkRequest request) {
		if (request == null || request.linkId() == null || request.linkId().isBlank() || request.code() == null) {
			throw new InvalidLinkCodeException();
		}
		UUID linkId;
		try {
			linkId = UUID.fromString(request.linkId());
		} catch (IllegalArgumentException exception) {
			throw new InvalidLinkCodeException();
		}
		ConfirmLinkResult result = confirmations.execute(linkId, request.code());
		return new ConfirmLinkResponse(true, result.accountNumber());
	}

}
