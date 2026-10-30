package com.mywallet.bank.api;

import com.mywallet.bank.application.command.AdminActor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

final class CurrentAdmin {

	private CurrentAdmin() {
	}

	static AdminActor get() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			throw new InsufficientAuthenticationException("Identité admin incomplète");
		}
		String id = jwt.getSubject();
		String username = jwt.getClaimAsString("preferred_username");
		if (id == null || id.isBlank() || username == null || username.isBlank()) {
			throw new InsufficientAuthenticationException("Identité admin incomplète");
		}
		return new AdminActor(id, username);
	}

}
