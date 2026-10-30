package com.mywallet.bank.application.service;

import com.mywallet.bank.domain.model.BankOperation;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.port.OperationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class InMemoryOperationRepository implements OperationRepository {

	private final Map<String, BankOperation> byReference = new HashMap<>();
	private int insertCount;

	int insertCount() {
		return insertCount;
	}

	@Override
	public Optional<BankOperation> findByReference(String reference) {
		return Optional.ofNullable(byReference.get(reference));
	}

	@Override
	public List<BankOperation> search(UUID accountId, OperationType type, OperationStatus status, int page, int size) {
		return List.of();
	}

	@Override
	public long countSearch(UUID accountId, OperationType type, OperationStatus status) {
		return 0;
	}

	@Override
	public BankOperation save(BankOperation operation) {
		insertCount++;
		byReference.put(operation.getReference(), operation);
		return operation;
	}

}
