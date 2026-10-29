package com.mywallet.bank.application.service;

import com.mywallet.bank.domain.model.BankOperation;
import com.mywallet.bank.domain.port.OperationRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
	public BankOperation save(BankOperation operation) {
		insertCount++;
		byReference.put(operation.getReference(), operation);
		return operation;
	}

}
