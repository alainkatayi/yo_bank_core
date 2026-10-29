package com.mywallet.bank.application.service;

import com.mywallet.bank.application.exception.OperationNotFoundException;
import com.mywallet.bank.application.port.in.GetOperationUseCase;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.port.OperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetOperationService implements GetOperationUseCase {

	private final OperationRepository operations;

	public GetOperationService(OperationRepository operations) {
		this.operations = operations;
	}

	@Override
	public OperationResult execute(String reference) {
		if (reference == null || reference.isBlank()) {
			throw new IllegalArgumentException("La référence est obligatoire");
		}
		return operations.findByReference(reference)
				.map(OperationResults::from)
				.orElseThrow(() -> new OperationNotFoundException(reference));
	}

}
