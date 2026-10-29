package com.mywallet.bank.application.service;

import java.time.Clock;
import java.time.Instant;

final class Timestamps {

	private Timestamps() {
	}

	static Instant now(Clock clock) {
		Instant instant = clock.instant();
		long microseconds = Math.round(instant.getNano() / 1000.0);
		long epochSecond = instant.getEpochSecond();
		if (microseconds == 1_000_000L) {
			epochSecond++;
			microseconds = 0L;
		}
		return Instant.ofEpochSecond(epochSecond, microseconds * 1_000L);
	}

}
