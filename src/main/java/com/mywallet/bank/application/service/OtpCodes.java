package com.mywallet.bank.application.service;

import java.security.SecureRandom;

final class OtpCodes {

	private OtpCodes() {
	}

	static String next(SecureRandom random) {
		return "%06d".formatted(random.nextInt(1_000_000));
	}

	static boolean isSixDigits(String code) {
		return code != null && code.matches("\\d{6}");
	}

}
