package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.BankOperation;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;

final class OperationMapper {

	BankOperation toDomain(OperationJpaEntity entity) {
		Money amount = Money.of(entity.getAmount());
		if (entity.getStatus() == OperationStatus.SUCCESS) {
			return BankOperation.success(
					entity.getId(),
					entity.getReference(),
					entity.getAccountId(),
					entity.getType(),
					amount,
					Money.of(entity.getBalanceAfter()),
					entity.getCreatedAt()
			);
		}
		return BankOperation.rejected(
				entity.getId(),
				entity.getReference(),
				entity.getAccountId(),
				entity.getType(),
				amount,
				entity.getRejectionReason(),
				entity.getCreatedAt()
		);
	}

	OperationJpaEntity toNewEntity(BankOperation operation) {
		return new OperationJpaEntity(
				operation.getId(),
				operation.getReference(),
				operation.getAccountId(),
				operation.getType(),
				operation.getAmount().amount(),
				operation.getStatus(),
				operation.getRejectionReason(),
				operation.getBalanceAfter() == null ? null : operation.getBalanceAfter().amount(),
				operation.getCreatedAt()
		);
	}

}
