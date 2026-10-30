package com.mywallet.bank.api;

import com.mywallet.bank.application.exception.InvalidCredentialsException;
import com.mywallet.bank.application.port.in.CreateAgentUseCase;
import com.mywallet.bank.application.port.in.ListAgentsUseCase;
import com.mywallet.bank.application.port.in.LoginAgentUseCase;
import com.mywallet.bank.application.result.AgentView;
import com.mywallet.bank.application.result.IssuedToken;
import com.mywallet.bank.application.result.LoginResult;
import com.mywallet.bank.domain.model.AgentRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankAgentController.class)
@WithBankSecurity
class BankAgentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private LoginAgentUseCase login;

	@MockitoBean
	private CreateAgentUseCase creation;

	@MockitoBean
	private ListAgentsUseCase agents;

	@Test
	void loginIsPublicAndReturnsABearerToken() throws Exception {
		when(login.execute(any())).thenReturn(new LoginResult(
				new IssuedToken("jeton-de-test", 1800),
				new AgentView(
						UUID.fromString("11111111-1111-1111-1111-111111111111"),
						"admin1",
						"Amina",
						"Admin",
						"admin1@bank.local",
						AgentRole.BANK_ADMIN,
						true,
						Instant.parse("2026-10-30T08:00:00Z")
				)
		));

		mockMvc.perform(post("/bank/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"admin1","password":"admin123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").value("jeton-de-test"))
				.andExpect(jsonPath("$.token_type").value("Bearer"))
				.andExpect(jsonPath("$.expires_in").value(1800))
				.andExpect(jsonPath("$.agent.username").value("admin1"))
				.andExpect(jsonPath("$.agent.firstName").value("Amina"))
				.andExpect(jsonPath("$.agent.lastName").value("Admin"))
				.andExpect(jsonPath("$.agent.email").value("admin1@bank.local"))
				.andExpect(jsonPath("$.agent.role").value("BANK_ADMIN"))
				.andExpect(jsonPath("$.agent.password").doesNotExist());
	}

	@Test
	void rejectsUnknownCredentialsWithoutEchoingThePassword() throws Exception {
		when(login.execute(any())).thenThrow(new InvalidCredentialsException());

		String body = mockMvc.perform(post("/bank/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"admin1","password":"mauvais-mot-de-passe"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertThat(body).doesNotContain("mauvais-mot-de-passe");
	}

}
