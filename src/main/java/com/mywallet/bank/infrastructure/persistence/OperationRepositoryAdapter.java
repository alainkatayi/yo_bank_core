package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.BankOperation;
import com.mywallet.bank.domain.port.OperationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class OperationRepositoryAdapter implements OperationRepository {

	private final SpringDataOperationRepository operations;
	private final OperationMapper mapper = new OperationMapper();

	public OperationRepositoryAdapter(SpringDataOperationRepository operations) {
		this.operations = operations;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<BankOperation> findByReference(String reference) {
		return operations.findByReference(reference).map(mapper::toDomain);
	}

	@Override
	@Transactional
	public BankOperation save(BankOperation operation) {
		return mapper.toDomain(operations.saveAndFlush(mapper.toNewEntity(operation)));
	}

}
