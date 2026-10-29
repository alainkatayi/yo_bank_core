package com.mywallet.bank.domain.model;

import com.mywallet.bank.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankOperationTest {

	private static final UUID ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
	private static final UUID ACCOUNT_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
	private static final UUID OTHER_ACCOUNT_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
	private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:15:00Z");
	private static final String REFERENCE = "OP-2026-0001";

	@Test
	void createsSuccessfulOperation() {
		BankOperation operation = BankOperation.success(
				ID,
				REFERENCE,
				ACCOUNT_ID,
				OperationType.DEBIT,
				Money.of("10.00"),
				Money.of("40.00"),
				CREATED_AT
		);

		assertThat(operation.getStatus()).isEqualTo(OperationStatus.SUCCESS);
		assertThat(operation.getRejectionReason()).isNull();
		assertThat(operation.getBalanceAfter()).isEqualTo(Money.of("40.00"));
		assertThat(operation.getAmount()).isEqualTo(Money.of("10.00"));
		assertThat(operation.getType()).isEqualTo(OperationType.DEBIT);
		assertThat(operation.getAccountId()).isEqualTo(ACCOUNT_ID);
		assertThat(operation.getReference()).isEqualTo(REFERENCE);
		assertThat(operation.getCreatedAt()).isEqualTo(CREATED_AT);
	}

	@Test
	void createsRejectedOperation() {
		BankOperation operation = BankOperation.rejected(
				ID,
				REFERENCE,
				ACCOUNT_ID,
				OperationType.CREDIT,
				Money.of("5.00"),
				RejectionReason.ACCOUNT_BLOCKED,
				CREATED_AT
		);

		assertThat(operation.getStatus()).isEqualTo(OperationStatus.REJECTED);
		assertThat(operation.getRejectionReason()).isEqualTo(RejectionReason.ACCOUNT_BLOCKED);
		assertThat(operation.getBalanceAfter()).isNull();
		assertThat(operation.getType()).isEqualTo(OperationType.CREDIT);
	}

	@Test
	void rejectsInvalidCombinations() {
		assertThatThrownBy(() -> BankOperation.success(
				ID, REFERENCE, ACCOUNT_ID, OperationType.DEBIT, Money.of("10.00"), null, CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> new BankOperation(
				ID,
				REFERENCE,
				ACCOUNT_ID,
				OperationType.DEBIT,
				Money.of("10.00"),
				OperationStatus.SUCCESS,
				RejectionReason.INSUFFICIENT_FUNDS,
				Money.of("40.00"),
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> BankOperation.rejected(
				ID, REFERENCE, ACCOUNT_ID, OperationType.DEBIT, Money.of("10.00"), null, CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> new BankOperation(
				ID,
				REFERENCE,
				ACCOUNT_ID,
				OperationType.DEBIT,
				Money.of("10.00"),
				OperationStatus.REJECTED,
				null,
				Money.of("40.00"),
				CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> BankOperation.success(
				ID, " ", ACCOUNT_ID, OperationType.DEBIT, Money.of("10.00"), Money.of("40.00"), CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> BankOperation.success(
				ID, "R".repeat(65), ACCOUNT_ID, OperationType.DEBIT, Money.of("10.00"), Money.of("40.00"), CREATED_AT
		)).isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> BankOperation.success(
				ID, REFERENCE, ACCOUNT_ID, OperationType.DEBIT, Money.zero(), Money.of("40.00"), CREATED_AT
		)).isInstanceOf(InvalidAmountException.class);
	}

	@Test
	void detectsTheSameRequest() {
		BankOperation operation = BankOperation.success(
				ID,
				REFERENCE,
				ACCOUNT_ID,
				OperationType.DEBIT,
				Money.of("10.00"),
				Money.of("40.00"),
				CREATED_AT
		);

		assertThat(operation.isSameRequestAs(ACCOUNT_ID, OperationType.DEBIT, Money.of("10"))).isTrue();
		assertThat(operation.isSameRequestAs(ACCOUNT_ID, OperationType.DEBIT, Money.of("10.01"))).isFalse();
		assertThat(operation.isSameRequestAs(ACCOUNT_ID, OperationType.CREDIT, Money.of("10.00"))).isFalse();
		assertThat(operation.isSameRequestAs(OTHER_ACCOUNT_ID, OperationType.DEBIT, Money.of("10.00"))).isFalse();
	}

}
