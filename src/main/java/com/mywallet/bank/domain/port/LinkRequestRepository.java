package com.mywallet.bank.domain.port;

import com.mywallet.bank.domain.model.LinkRequest;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LinkRequestRepository {

	void lockAccountNumber(String accountNumber);

	int countCreatedSince(String accountNumber, Instant since);

	Instant oldestCreatedSince(String accountNumber, Instant since);

	void cancelPending(String accountNumber);

	LinkRequest save(LinkRequest request);

	Optional<LinkRequest> findById(UUID id);

	boolean confirmIfPending(UUID id, Instant now, String codeHash);

	boolean registerFailedAttempt(UUID id, Instant now);

	boolean expireIfPending(UUID id, Instant now);

}
