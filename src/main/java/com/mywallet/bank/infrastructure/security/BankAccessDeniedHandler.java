package com.mywallet.bank.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Component
public class BankAccessDeniedHandler implements AccessDeniedHandler {

	private final JsonMapper jsonMapper;

	public BankAccessDeniedHandler(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException
	) throws IOException {
		SecurityProblems.write(
				request,
				response,
				HttpStatus.FORBIDDEN,
				"FORBIDDEN",
				"Accès refusé",
				jsonMapper
		);
	}

}
