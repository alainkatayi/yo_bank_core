package com.mywallet.bank.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class BankSecurityConfiguration {

	@Bean
	SecurityFilterChain bankSecurityFilterChain(
			HttpSecurity http,
			BankJwtAuthenticationConverter jwtAuthenticationConverter,
			BankAuthenticationEntryPoint authenticationEntryPoint,
			BankAccessDeniedHandler accessDeniedHandler
	) throws Exception {
		http
				// API à jeton Bearer, sans cookie de session : le CSRF des formulaires ne s'applique pas.
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.cors(Customizer.withDefaults())
				.exceptionHandling(handler -> handler
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/bank/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/bank/auth/login").permitAll()
						.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/bank/operations/debit").hasAuthority("SCOPE_bank:debit")
						.requestMatchers(HttpMethod.POST, "/bank/operations/credit").hasAuthority("SCOPE_bank:credit")
						.requestMatchers(HttpMethod.GET, "/bank/operations/**").hasAuthority("SCOPE_bank:read")
						.requestMatchers(HttpMethod.GET, "/bank/accounts/**").hasAuthority("SCOPE_bank:read")
						.requestMatchers(HttpMethod.POST, "/bank/links/verify").hasAuthority("SCOPE_bank:read")
						.requestMatchers(HttpMethod.GET, "/bank/admin/**").hasAnyRole("BANK_ADMIN", "BANK_AUDITOR")
						.requestMatchers("/bank/admin/**").hasRole("BANK_ADMIN")
						.anyRequest().denyAll())
				.oauth2ResourceServer(oauth -> oauth
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
						.authenticationEntryPoint(authenticationEntryPoint));
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:4200"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	JwtDecoder jwtDecoder(
			@Value("${bank.security.jwt.secret}") String secret,
			@Value("${bank.security.jwt.issuer}") String issuer,
			@Value("${bank.security.jwt.keycloak-issuer}") String keycloakIssuer
	) {
		JwtDecoder local = NimbusJwtDecoder.withSecretKey(BankJwtIssuer.secretKey(secret)).build();
		return new BankJwtDecoder(local, issuer, keycloakIssuer);
	}

}
