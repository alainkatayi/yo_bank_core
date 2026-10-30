package com.mywallet.bank.domain.model;

public enum AgentRole {
	BANK_ADMIN("bank-admin"),
	BANK_AUDITOR("bank-auditor");

	private final String tokenName;

	AgentRole(String tokenName) {
		this.tokenName = tokenName;
	}

	public String tokenName() {
		return tokenName;
	}

}
