package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.BankAccount;
import com.mywallet.bank.domain.model.BankOperation;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.model.RejectionReason;
import com.mywallet.bank.domain.port.OperationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationRepositoryAdapterTest extends AbstractPersistenceIntegrationTest {

	private static final Instant CREATED_AT = Instant.parse("2026-01-15T12:00:00Z");

	@Autowired
	private OperationRepository operationRepository;

	@Test
	void savesSuccessAndRejectedOperationsAndReadsThemByReference() {
		BankAccount account = persistedAccount(Money.of("100.00"));
		String successReference = "OP-" + UUID.randomUUID();
		String rejectedReference = "OP-" + UUID.randomUUID();
		BankOperation success = BankOperation.success(
				UUID.randomUUID(),
				successReference,
				account.getId(),
				OperationType.DEBIT,
				Money.of("15.50"),
				Money.of("84.50"),
				CREATED_AT
		);
		BankOperation rejected = BankOperation.rejected(
				UUID.randomUUID(),
				rejectedReference,
				account.getId(),
				OperationType.CREDIT,
				Money.of("200.00"),
				RejectionReason.INSUFFICIENT_FUNDS,
				CREATED_AT
		);

		operationRepository.save(success);
		operationRepository.save(rejected);

		BankOperation loadedSuccess = operationRepository.findByReference(successReference).orElseThrow();
		assertThat(loadedSuccess.getId()).isEqualTo(success.getId());
		assertThat(loadedSuccess.getReference()).isEqualTo(successReference);
		assertThat(loadedSuccess.getAccountId()).isEqualTo(account.getId());
		assertThat(loadedSuccess.getType()).isEqualTo(OperationType.DEBIT);
		assertThat(loadedSuccess.getAmount()).isEqualTo(Money.of("15.50"));
		assertThat(loadedSuccess.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(loadedSuccess.getRejectionReason()).isNull();
		assertThat(loadedSuccess.getBalanceAfter()).isEqualTo(Money.of("84.50"));
		assertThat(loadedSuccess.getCreatedAt()).isEqualTo(CREATED_AT);

		BankOperation loadedRejected = operationRepository.findByReference(rejectedReference).orElseThrow();
		assertThat(loadedRejected.getId()).isEqualTo(rejected.getId());
		assertThat(loadedRejected.getReference()).isEqualTo(rejectedReference);
		assertThat(loadedRejected.getAccountId()).isEqualTo(account.getId());
		assertThat(loadedRejected.getType()).isEqualTo(OperationType.CREDIT);
		assertThat(loadedRejected.getAmount()).isEqualTo(Money.of("200.00"));
		assertThat(loadedRejected.getStatus()).isEqualTo(OperationStatus.REJECTED);
		assertThat(loadedRejected.getRejectionReason()).isEqualTo(RejectionReason.INSUFFICIENT_FUNDS);
		assertThat(loadedRejected.getBalanceAfter()).isNull();
		assertThat(loadedRejected.getCreatedAt()).isEqualTo(CREATED_AT);
	}

	@Test
	void rejectsADuplicateReference() {
		BankAccount account = persistedAccount(Money.of("50.00"));
		String reference = "OP-" + UUID.randomUUID();
		operationRepository.save(BankOperation.success(
				UUID.randomUUID(),
				reference,
				account.getId(),
				OperationType.CREDIT,
				Money.of("10.00"),
				Money.of("60.00"),
				CREATED_AT
		));

		assertThatThrownBy(() -> operationRepository.save(BankOperation.success(
				UUID.randomUUID(),
				reference,
				account.getId(),
				OperationType.CREDIT,
				Money.of("1.00"),
				Money.of("61.00"),
				CREATED_AT
		))).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void returnsEmptyWhenTheReferenceIsUnknown() {
		assertThat(operationRepository.findByReference("OP-does-not-exist")).isEmpty();
	}

}
