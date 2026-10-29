package com.mywallet.bank.domain.port;

import com.mywallet.bank.domain.model.BankOperation;

import java.util.Optional;

public interface OperationRepository {

	Optional<BankOperation> findByReference(String reference);

	BankOperation save(BankOperation operation);

}
