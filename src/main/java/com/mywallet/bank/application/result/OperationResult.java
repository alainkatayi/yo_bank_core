package com.mywallet.bank.application.result;

import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.model.RejectionReason;

import java.time.Instant;

public record OperationResult(
		String reference,
		OperationType type,
		OperationStatus status,
		RejectionReason rejectionReason,
		Money balanceAfter,
		Instant createdAt
) {

	public OperationResult {
		if (reference == null || reference.isBlank()) {
			throw new IllegalArgumentException("La référence est obligatoire");
		}
		if (type == null || status == null || createdAt == null) {
			throw new IllegalArgumentException("Le résultat d'opération est incomplet");
		}
		if (status == OperationStatus.SUCCESS && (balanceAfter == null || rejectionReason != null)) {
			throw new IllegalArgumentException("Un succès exige le solde après opération et aucun motif de rejet");
		}
		if (status == OperationStatus.REJECTED && rejectionReason == null) {
			throw new IllegalArgumentException("Un refus exige un motif de rejet");
		}
	}

}
