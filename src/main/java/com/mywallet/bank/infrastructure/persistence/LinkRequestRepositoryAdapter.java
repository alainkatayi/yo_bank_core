package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.LinkRequest;
import com.mywallet.bank.domain.model.LinkStatus;
import com.mywallet.bank.domain.port.LinkRequestRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LinkRequestRepositoryAdapter implements LinkRequestRepository {

	private final SpringDataLinkRequestRepository requests;
	private final JdbcTemplate jdbc;

	public LinkRequestRepositoryAdapter(SpringDataLinkRequestRepository requests, JdbcTemplate jdbc) {
		this.requests = requests;
		this.jdbc = jdbc;
	}

	@Override
	public void lockAccountNumber(String accountNumber) {
		jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {
			try (java.sql.PreparedStatement statement = connection.prepareStatement(
					"SELECT pg_advisory_xact_lock(hashtextextended(?, 0::bigint))"
			)) {
				statement.setString(1, accountNumber);
				statement.execute();
			}
			return null;
		});
	}

	@Override
	public int countCreatedSince(String accountNumber, Instant since) {
		Integer count = jdbc.queryForObject(
				"SELECT count(*) FROM link_request WHERE account_number = ? AND created_at > ?",
				Integer.class,
				accountNumber,
				Timestamp.from(since)
		);
		return count == null ? 0 : count;
	}

	@Override
	public Instant oldestCreatedSince(String accountNumber, Instant since) {
		return jdbc.queryForObject(
				"SELECT min(created_at) FROM link_request WHERE account_number = ? AND created_at > ?",
				(result, row) -> {
					Timestamp value = result.getTimestamp(1);
					return value == null ? null : value.toInstant();
				},
				accountNumber,
				Timestamp.from(since)
		);
	}

	@Override
	public void cancelPending(String accountNumber) {
		jdbc.update(
				"UPDATE link_request SET status = 'CANCELLED' WHERE account_number = ? AND status = 'PENDING'",
				accountNumber
		);
	}

	@Override
	public LinkRequest save(LinkRequest request) {
		LinkRequestJpaEntity entity = new LinkRequestJpaEntity(
				request.getId(),
				request.getAccountNumber(),
				request.getAccountId(),
				request.isEligible(),
				request.getCodeHash(),
				request.getAttempts(),
				request.getStatus(),
				request.getExpiresAt(),
				request.getConfirmedAt(),
				request.getCreatedAt()
		);
		return toDomain(requests.saveAndFlush(entity));
	}

	@Override
	public Optional<LinkRequest> findById(UUID id) {
		return requests.findById(id).map(LinkRequestRepositoryAdapter::toDomain);
	}

	@Override
	public boolean confirmIfPending(UUID id, Instant now, String codeHash) {
		int updated = jdbc.update("""
				UPDATE link_request
				SET status = 'CONFIRMED', confirmed_at = ?
				WHERE id = ?
				  AND status = 'PENDING'
				  AND eligible
				  AND expires_at > ?
				  AND attempts < 3
				  AND code_hash = ?
				""",
				Timestamp.from(now),
				id,
				Timestamp.from(now),
				codeHash
		);
		return updated == 1;
	}

	@Override
	public boolean registerFailedAttempt(UUID id, Instant now) {
		int updated = jdbc.update("""
				UPDATE link_request
				SET attempts = attempts + 1,
				    status = CASE WHEN attempts + 1 >= 3 THEN 'CANCELLED' ELSE status END
				WHERE id = ?
				  AND status = 'PENDING'
				  AND eligible
				  AND expires_at > ?
				""",
				id,
				Timestamp.from(now)
		);
		return updated == 1;
	}

	@Override
	public boolean expireIfPending(UUID id, Instant now) {
		int updated = jdbc.update("""
				UPDATE link_request
				SET status = 'EXPIRED'
				WHERE id = ?
				  AND status = 'PENDING'
				  AND expires_at <= ?
				""",
				id,
				Timestamp.from(now)
		);
		return updated == 1;
	}

	private static LinkRequest toDomain(LinkRequestJpaEntity entity) {
		String hash = entity.getCodeHash();
		if (hash != null) {
			hash = hash.trim();
		}
		return new LinkRequest(
				entity.getId(),
				entity.getAccountNumber().trim(),
				entity.getAccountId(),
				entity.isEligible(),
				hash,
				entity.getAttempts(),
				entity.getStatus() == null ? LinkStatus.PENDING : entity.getStatus(),
				entity.getExpiresAt(),
				entity.getConfirmedAt(),
				entity.getCreatedAt()
		);
	}

}
