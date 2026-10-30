package com.mywallet.bank.domain.port;

import com.mywallet.bank.domain.model.BankOperation;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OperationRepository {

	Optional<BankOperation> findByReference(String reference);

	List<BankOperation> search(UUID accountId, OperationType type, OperationStatus status, int page, int size);

	long countSearch(UUID accountId, OperationType type, OperationStatus status);

	BankOperation save(BankOperation operation);

}
