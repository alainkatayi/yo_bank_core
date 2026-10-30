package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.BankOperation;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.port.OperationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
	@Transactional(readOnly = true)
	public List<BankOperation> search(UUID accountId, OperationType type, OperationStatus status, int page, int size) {
		return operations.search(
						accountId,
						type != null,
						type == null ? OperationType.DEBIT : type,
						status != null,
						status == null ? OperationStatus.SUCCESS : status,
						PageRequest.of(page, size)
				).stream()
				.map(mapper::toDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public long countSearch(UUID accountId, OperationType type, OperationStatus status) {
		return operations.countSearch(
				accountId,
				type != null,
				type == null ? OperationType.DEBIT : type,
				status != null,
				status == null ? OperationStatus.SUCCESS : status
		);
	}

	@Override
	@Transactional
	public BankOperation save(BankOperation operation) {
		return mapper.toDomain(operations.saveAndFlush(mapper.toNewEntity(operation)));
	}

}
