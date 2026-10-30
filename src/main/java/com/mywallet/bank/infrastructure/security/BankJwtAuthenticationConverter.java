package com.mywallet.bank.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

@Component
public class BankJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		Collection<GrantedAuthority> scopeAuthorities = scopes.convert(jwt);
		if (scopeAuthorities != null) {
			authorities.addAll(scopeAuthorities);
		}
		Object realmAccess = jwt.getClaim("realm_access");
		if (realmAccess instanceof java.util.Map<?, ?> access) {
			Object roles = access.get("roles");
			if (roles instanceof Collection<?> collection) {
				for (Object role : collection) {
					if (role instanceof String name && !name.isBlank()) {
						authorities.add(new SimpleGrantedAuthority(roleAuthority(name)));
					}
				}
			}
		}
		String name = jwt.getClaimAsString("preferred_username");
		if (name == null || name.isBlank()) {
			name = jwt.getSubject();
		}
		return new JwtAuthenticationToken(jwt, authorities, name);
	}

	static String roleAuthority(String role) {
		return "ROLE_" + role.toUpperCase(Locale.ROOT).replace('-', '_');
	}

}
