package com.mywallet.bank.api;

import com.jayway.jsonpath.JsonPath;
import com.mywallet.bank.application.service.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class BankAgentApiIntegrationTest extends AbstractApplicationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void logsInAnAgentStoredInTheBankDatabaseAndListsAgents() throws Exception {
		String token = login("admin1", "admin123");

		mockMvc.perform(get("/bank/admin/agents").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[?(@.username == 'admin1')].role").value("BANK_ADMIN"))
				.andExpect(jsonPath("$.[?(@.username == 'auditor1')].role").value("BANK_AUDITOR"))
				.andExpect(jsonPath("$[*].password").doesNotExist())
				.andExpect(jsonPath("$[*].passwordHash").doesNotExist());
	}

	@Test
	void rejectsAWrongPassword() throws Exception {
		String body = mockMvc.perform(post("/bank/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"admin1","password":"pas-le-bon"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertThat(body).doesNotContain("pas-le-bon");
	}

	@Test
	void createsAnAgentThenLetsThatAgentLogIn() throws Exception {
		String admin = login("admin1", "admin123");
		String username = "agent" + UUID.randomUUID().toString().substring(0, 8);
		String created = mockMvc.perform(post("/bank/admin/agents")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + admin)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","password":"agent12345","firstName":"Claire","lastName":"Agent","email":"claire@bank.local","role":"BANK_ADMIN"}
								""".formatted(username)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value(username))
				.andExpect(jsonPath("$.role").value("BANK_ADMIN"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String id = JsonPath.read(created, "$.id");
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from admin_audit_log where action = 'AGENT_CREATED' and target_id = ?",
				Integer.class,
				id
		)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"select details from admin_audit_log where action = 'AGENT_CREATED' and target_id = ?",
				String.class,
				id
		)).isEqualTo("username=" + username);

		String agentToken = login(username, "agent12345");
		mockMvc.perform(get("/bank/admin/agents").header(HttpHeaders.AUTHORIZATION, "Bearer " + agentToken))
				.andExpect(status().isOk());

		mockMvc.perform(post("/bank/admin/agents")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + login("auditor1", "auditor123"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"autre","password":"agent12345","firstName":"No","lastName":"Access","role":"BANK_AUDITOR"}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	private String login(String username, String password) throws Exception {
		String body = mockMvc.perform(post("/bank/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","password":"%s"}
								""".formatted(username, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token_type").value("Bearer"))
				.andExpect(jsonPath("$.agent.username").value(username))
				.andExpect(jsonPath("$.agent.password").doesNotExist())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return JsonPath.read(body, "$.access_token");
	}

}
