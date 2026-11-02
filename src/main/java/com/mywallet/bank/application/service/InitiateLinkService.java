package com.mywallet.bank.application.service;

import com.mywallet.bank.application.exception.InvalidQueryException;
import com.mywallet.bank.application.port.in.InitiateLinkUseCase;
import com.mywallet.bank.application.port.out.OtpSender;
import com.mywallet.bank.application.result.InitiateLinkResult;
import org.springframework.stereotype.Service;

/**
 * L'envoi part après le commit de {@link InitiateLinkExecutor}.
 * Il ne bloque pas la réponse : le temps SMTP ne doit pas distinguer
 * une demande éligible d'une demande refusée.
 */
@Service
public class InitiateLinkService implements InitiateLinkUseCase {

	private final InitiateLinkExecutor executor;
	private final OtpSender sender;

	public InitiateLinkService(InitiateLinkExecutor executor, OtpSender sender) {
		this.executor = executor;
		this.sender = sender;
	}

	@Override
	public InitiateLinkResult execute(String accountNumber, String phone) {
		String normalized = PhoneNumbers.normalize(phone);
		if (normalized.isEmpty()) {
			throw new InvalidQueryException();
		}
		PreparedLink prepared = executor.open(accountNumber, normalized);
		if (prepared.code() != null) {
			sender.send(prepared.linkId(), prepared.email(), prepared.code());
		}
		return new InitiateLinkResult(prepared.linkId(), prepared.expiresIn());
	}

}
