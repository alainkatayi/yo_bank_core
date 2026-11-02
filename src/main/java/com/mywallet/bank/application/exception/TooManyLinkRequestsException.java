package com.mywallet.bank.application.exception;

public final class TooManyLinkRequestsException extends RuntimeException {

	private final int retryAfterSeconds;

	public TooManyLinkRequestsException(int retryAfterSeconds) {
		super("Trop de demandes de liaison");
		this.retryAfterSeconds = Math.max(retryAfterSeconds, 1);
	}

	public int retryAfterSeconds() {
		return retryAfterSeconds;
	}

}
