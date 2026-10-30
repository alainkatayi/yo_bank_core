package com.mywallet.bank.infrastructure.security;

import com.nimbusds.jwt.JWTParser;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.text.ParseException;

final class BankJwtDecoder implements JwtDecoder {

	private final JwtDecoder local;
	private final String localIssuer;
	private final String keycloakIssuer;
	private volatile JwtDecoder keycloak;

	BankJwtDecoder(JwtDecoder local, String localIssuer, String keycloakIssuer) {
		this.local = local;
		this.localIssuer = localIssuer;
		this.keycloakIssuer = keycloakIssuer;
	}

	@Override
	public Jwt decode(String token) {
		String issuer = issuerOf(token);
		if (localIssuer.equals(issuer)) {
			return local.decode(token);
		}
		if (keycloakIssuer.equals(issuer)) {
			return keycloakDecoder().decode(token);
		}
		throw new BadJwtException("Émetteur inconnu");
	}

	private JwtDecoder keycloakDecoder() {
		JwtDecoder current = keycloak;
		if (current == null) {
			synchronized (this) {
				current = keycloak;
				if (current == null) {
					current = NimbusJwtDecoder.withIssuerLocation(keycloakIssuer).build();
					keycloak = current;
				}
			}
		}
		return current;
	}

	private static String issuerOf(String token) {
		try {
			return JWTParser.parse(token).getJWTClaimsSet().getIssuer();
		} catch (ParseException exception) {
			throw new BadJwtException("Jeton illisible");
		}
	}

}
