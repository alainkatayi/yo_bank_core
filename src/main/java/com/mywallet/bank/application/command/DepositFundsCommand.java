package com.mywallet.bank.application.command;

public record DepositFundsCommand(AdminActor actor, String accountNumber, String amount) {

	public DepositFundsCommand {
		if (actor == null) {
			throw new IllegalArgumentException("L'acteur est obligatoire");
		}
	}

}
