package com.mywallet.bank.application.port.out;

public interface PasswordChecker {

	boolean matches(String rawPassword, String passwordHash);

	String hash(String rawPassword);

}
