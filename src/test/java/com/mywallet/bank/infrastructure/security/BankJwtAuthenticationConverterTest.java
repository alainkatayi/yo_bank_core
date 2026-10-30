package com.mywallet.bank.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BankJwtAuthenticationConverterTest {

	@Test
	void keepsScopesAndMapsDashedRealmRoles() {
		Jwt jwt = jwt(Map.of("roles", List.of("bank-admin", "bank-auditor")), true);

		assertThat(authorities(jwt)).containsExactlyInAnyOrder(
				"SCOPE_bank:read",
				"ROLE_BANK_ADMIN",
				"ROLE_BANK_AUDITOR"
		);
	}

	@Test
	void ignoresAMissingRealmAccessClaim() {
		assertThat(authorities(jwt(null, false))).containsExactly("SCOPE_bank:debit");
	}

	@Test
	void ignoresAnEmptyRoleList() {
		assertThat(authorities(jwt(Map.of("roles", List.of()), true))).containsExactly("SCOPE_bank:credit");
	}

	private static List<String> authorities(Jwt jwt) {
		return new BankJwtAuthenticationConverter().convert(jwt).getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();
	}

	private static Jwt jwt(Map<String, Object> realmAccess, boolean includeRealmAccess) {
		String scope = includeRealmAccess
				? (realmAccess.get("roles") instanceof List<?> roles && roles.isEmpty() ? "bank:credit" : "bank:read")
				: "bank:debit";
		Jwt.Builder builder = Jwt.withTokenValue("token")
				.header("alg", "none")
				.subject("subject")
				.claim("preferred_username", "someone")
				.claim("scope", scope)
				.issuedAt(Instant.parse("2026-10-30T08:00:00Z"))
				.expiresAt(Instant.parse("2026-10-30T09:00:00Z"));
		if (includeRealmAccess) {
			builder.claim("realm_access", realmAccess);
		}
		return builder.build();
	}

}
