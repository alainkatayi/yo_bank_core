package com.mywallet.bank.application.service;

import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Locale;

final class UniqueConstraints {

	private UniqueConstraints() {
	}

	static boolean matches(DataIntegrityViolationException exception, String marker) {
		String needle = marker.toLowerCase(Locale.ROOT);
		Throwable current = exception;
		while (current != null) {
			if (current instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
				if (contains(sql.getMessage(), needle)) {
					return true;
				}
			}
			if (contains(current.getMessage(), needle)) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private static boolean contains(String message, String needle) {
		return message != null && message.toLowerCase(Locale.ROOT).contains(needle);
	}

}
