package com.mywallet.bank.infrastructure.security;

import com.mywallet.bank.application.port.out.AccessTokenIssuer;
import com.mywallet.bank.application.result.IssuedToken;
import com.mywallet.bank.domain.model.BankAgent;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class BankJwtIssuer implements AccessTokenIssuer {

	private final NimbusJwtEncoder encoder;
	private final String issuer;
	private final long ttlSeconds;
	private final Clock clock;

	public BankJwtIssuer(
			@Value("${bank.security.jwt.secret}") String secret,
			@Value("${bank.security.jwt.issuer}") String issuer,
			@Value("${bank.security.jwt.ttl-seconds}") long ttlSeconds,
			Clock clock
	) {
		this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(secret)));
		this.issuer = issuer;
		this.ttlSeconds = ttlSeconds;
		this.clock = clock;
	}

	@Override
	public IssuedToken issue(BankAgent agent) {
		Instant issuedAt = clock.instant();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.subject(agent.getId().toString())
				.claim("preferred_username", agent.getUsername())
				.claim("realm_access", Map.of("roles", List.of(agent.getRole().tokenName())))
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plusSeconds(ttlSeconds))
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new IssuedToken(value, ttlSeconds);
	}

	static SecretKey secretKey(String secret) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("La clé de signature des jetons est trop courte");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

}
