package com.mywallet.bank.application.service;

import com.mywallet.bank.application.exception.InvalidLinkCodeException;
import com.mywallet.bank.application.port.in.ConfirmLinkUseCase;
import com.mywallet.bank.application.port.out.OtpHasher;
import com.mywallet.bank.application.result.ConfirmLinkResult;
import com.mywallet.bank.domain.model.LinkRequest;
import com.mywallet.bank.domain.model.LinkStatus;
import com.mywallet.bank.domain.port.LinkRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class ConfirmLinkService implements ConfirmLinkUseCase {

	static final int MAX_ATTEMPTS = 3;

	private final LinkRequestRepository links;
	private final OtpHasher hasher;
	private final Clock clock;

	public ConfirmLinkService(LinkRequestRepository links, OtpHasher hasher, Clock clock) {
		this.links = links;
		this.hasher = hasher;
		this.clock = clock;
	}

	@Override
	@Transactional
	public ConfirmLinkResult execute(UUID linkId, String code) {
		LinkRequest request = links.findById(linkId).orElseThrow(InvalidLinkCodeException::new);
		Instant now = Timestamps.now(clock);
		if (!request.getExpiresAt().isAfter(now)) {
			links.expireIfPending(request.getId(), now);
			throw new InvalidLinkCodeException();
		}
		if (!request.isEligible()
				|| request.getStatus() != LinkStatus.PENDING
				|| request.getCodeHash() == null
				|| request.getAttempts() >= MAX_ATTEMPTS) {
			throw new InvalidLinkCodeException();
		}
		boolean matches = OtpCodes.isSixDigits(code) && hasher.matches(request.getId(), code, request.getCodeHash());
		if (matches && links.confirmIfPending(request.getId(), now, request.getCodeHash())) {
			return new ConfirmLinkResult(request.getAccountNumber());
		}
		if (!matches) {
			links.registerFailedAttempt(request.getId(), now);
		}
		throw new InvalidLinkCodeException();
	}

}
