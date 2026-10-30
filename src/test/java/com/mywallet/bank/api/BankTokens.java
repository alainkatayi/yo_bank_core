package com.mywallet.bank.api;

import com.mywallet.bank.application.command.AdminActor;
import com.mywallet.bank.infrastructure.security.BankJwtAuthenticationConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public final class BankTokens {

	public static final String ADMIN_ID = "11111111-1111-1111-1111-111111111111";
	public static final String AUDITOR_ID = "22222222-2222-2222-2222-222222222222";
	public static final AdminActor ACTOR = new AdminActor(ADMIN_ID, "admin1");

	private static final BankJwtAuthenticationConverter CONVERTER = new BankJwtAuthenticationConverter();

	private BankTokens() {
	}

	public static RequestPostProcessor admin() {
		return token(ADMIN_ID, "admin1", List.of("bank-admin"), List.of());
	}

	public static RequestPostProcessor auditor() {
		return token(AUDITOR_ID, "auditor1", List.of("bank-auditor"), List.of());
	}

	public static RequestPostProcessor walletDebit() {
		return wallet("bank:debit");
	}

	public static RequestPostProcessor walletCredit() {
		return wallet("bank:credit");
	}

	public static RequestPostProcessor walletRead() {
		return wallet("bank:read");
	}

	public static RequestPostProcessor walletAll() {
		return wallet("bank:debit", "bank:credit", "bank:read");
	}

	public static RequestPostProcessor wallet(String... scopes) {
		return token("service-account-wallet-service", "service-account-wallet-service", List.of(), List.of(scopes));
	}

	private static RequestPostProcessor token(
			String subject,
			String username,
			List<String> roles,
			List<String> scopes
	) {
		return jwt()
				.jwt(builder -> {
					builder.subject(subject)
							.claim("preferred_username", username)
							.issuedAt(Instant.parse("2026-10-30T08:00:00Z"))
							.expiresAt(Instant.parse("2026-10-30T09:00:00Z"));
					if (!roles.isEmpty()) {
						builder.claim("realm_access", Map.of("roles", roles));
					}
					if (!scopes.isEmpty()) {
						builder.claim("scope", String.join(" ", scopes));
					}
				})
				.authorities(BankTokens::authorities);
	}

	private static Collection<GrantedAuthority> authorities(Jwt jwt) {
		return CONVERTER.convert(jwt).getAuthorities();
	}

}
