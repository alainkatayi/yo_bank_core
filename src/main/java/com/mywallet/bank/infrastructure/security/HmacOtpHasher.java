package com.mywallet.bank.infrastructure.security;

import com.mywallet.bank.application.port.out.OtpHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class HmacOtpHasher implements OtpHasher {

	private static final String ALGORITHM = "HmacSHA256";
	private static final byte[] ZEROS = new byte[32];

	private final byte[] secret;

	public HmacOtpHasher(@Value("${app.otp.secret}") String secret) {
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public String hash(UUID linkId, String code) {
		return HexFormat.of().formatHex(hmac(linkId, code));
	}

	@Override
	public boolean matches(UUID linkId, String code, String storedHash) {
		return MessageDigest.isEqual(hmac(linkId, code), decode(storedHash));
	}

	private byte[] hmac(UUID linkId, String code) {
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(new SecretKeySpec(secret, ALGORITHM));
			return mac.doFinal((linkId + ":" + code).getBytes(StandardCharsets.UTF_8));
		} catch (Exception exception) {
			throw new IllegalStateException("Calcul d'empreinte impossible");
		}
	}

	private static byte[] decode(String storedHash) {
		if (storedHash == null || !storedHash.matches("[0-9a-f]{64}")) {
			return ZEROS;
		}
		return HexFormat.of().parseHex(storedHash);
	}

}
