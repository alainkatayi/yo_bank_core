package com.mywallet.bank.application.port.out;

import java.util.UUID;

public interface OtpHasher {

	String hash(UUID linkId, String code);

	boolean matches(UUID linkId, String code, String storedHash);

}
