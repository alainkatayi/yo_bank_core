package com.mywallet.bank.application.service;

import com.mywallet.bank.application.exception.TooManyLinkRequestsException;
import com.mywallet.bank.application.port.out.OtpHasher;
import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.Customer;
import com.mywallet.bank.domain.model.CustomerStatus;
import com.mywallet.bank.domain.model.LinkRequest;
import com.mywallet.bank.domain.model.LinkStatus;
import com.mywallet.bank.domain.port.AccountRepository;
import com.mywallet.bank.domain.port.CustomerRepository;
import com.mywallet.bank.domain.port.LinkRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class InitiateLinkExecutor {

	static final int TTL_SECONDS = 300;
	static final int MAX_REQUESTS_PER_HOUR = 3;

	private final AccountRepository accounts;
	private final CustomerRepository customers;
	private final LinkRequestRepository links;
	private final OtpHasher hasher;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();

	public InitiateLinkExecutor(
			AccountRepository accounts,
			CustomerRepository customers,
			LinkRequestRepository links,
			OtpHasher hasher,
			Clock clock
	) {
		this.accounts = accounts;
		this.customers = customers;
		this.links = links;
		this.hasher = hasher;
		this.clock = clock;
	}

	@Transactional
	public PreparedLink open(String accountNumber, String normalizedPhone) {
		links.lockAccountNumber(accountNumber);
		Instant now = Timestamps.now(clock);
		Instant since = now.minus(Duration.ofHours(1));
		if (links.countCreatedSince(accountNumber, since) >= MAX_REQUESTS_PER_HOUR) {
			throw new TooManyLinkRequestsException(retryAfter(now, links.oldestCreatedSince(accountNumber, since)));
		}
		Assessment assessment = assess(accountNumber, normalizedPhone);
		UUID id = UUID.randomUUID();
		String code = null;
		String hash = null;
		if (assessment.eligible()) {
			code = OtpCodes.next(random);
			hash = hasher.hash(id, code);
			links.cancelPending(accountNumber);
		} else {
			hasher.hash(id, "000000");
		}
		links.save(new LinkRequest(
				id,
				accountNumber,
				assessment.accountId(),
				assessment.eligible(),
				hash,
				0,
				LinkStatus.PENDING,
				now.plusSeconds(TTL_SECONDS),
				null,
				now
		));
		return new PreparedLink(id, TTL_SECONDS, assessment.email(), code);
	}

	private Assessment assess(String accountNumber, String normalizedPhone) {
		BankAccount account = accounts.findByAccountNumber(new AccountNumber(accountNumber)).orElse(null);
		if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
			return Assessment.ineligible(account == null ? null : account.getId());
		}
		Customer customer = customers.findById(account.getCustomerId()).orElse(null);
		if (customer == null || customer.getStatus() != CustomerStatus.ACTIVE) {
			return Assessment.ineligible(account.getId());
		}
		String email = customer.getEmail();
		boolean phoneMatches = normalizedPhone.equals(PhoneNumbers.normalize(customer.getPhone()));
		if (!phoneMatches || email == null || email.isBlank()) {
			return Assessment.ineligible(account.getId());
		}
		return new Assessment(true, account.getId(), email);
	}

	private static int retryAfter(Instant now, Instant oldest) {
		if (oldest == null) {
			return 3600;
		}
		long seconds = Duration.between(now, oldest.plus(Duration.ofHours(1))).toSeconds();
		return (int) Math.max(seconds, 1);
	}

	private record Assessment(boolean eligible, UUID accountId, String email) {

		static Assessment ineligible(UUID accountId) {
			return new Assessment(false, accountId, null);
		}

	}

}
