package com.mywallet.bank.infrastructure.security;

import com.mywallet.bank.application.port.out.PasswordChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordChecker implements PasswordChecker {

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		return encoder.matches(rawPassword, passwordHash);
	}

	@Override
	public String hash(String rawPassword) {
		return encoder.encode(rawPassword);
	}

}
