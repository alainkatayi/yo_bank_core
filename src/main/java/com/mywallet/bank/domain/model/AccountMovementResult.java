package com.mywallet.bank.domain.model;

public sealed interface AccountMovementResult
		permits AccountMovementResult.Accepted, AccountMovementResult.Rejected {

	record Accepted(Money balanceAfter) implements AccountMovementResult {
		public Accepted {
			if (balanceAfter == null) {
				throw new IllegalArgumentException("Le solde après opération est obligatoire");
			}
		}
	}

	record Rejected(RejectionReason reason) implements AccountMovementResult {
		public Rejected {
			if (reason == null) {
				throw new IllegalArgumentException("Le motif de rejet est obligatoire");
			}
		}
	}

}
