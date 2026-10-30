package com.mywallet.bank.api;

import com.mywallet.bank.application.port.in.VerifyLinkUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankLinkController.class)
@WithBankSecurity
class BankLinkControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VerifyLinkUseCase links;

	@Test
	void returnsVerifiedWhenTheUseCaseAcceptsThePair() throws Exception {
		when(links.execute("123456789012", "243810000001")).thenReturn(true);

		mockMvc.perform(post("/bank/links/verify").with(BankTokens.walletRead())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountNumber":"123456789012","phone":"243810000001"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.verified").value(true));
	}

	@Test
	void returnsNotVerifiedWhenTheUseCaseRefusesThePair() throws Exception {
		when(links.execute("123456789012", "243810000002")).thenReturn(false);

		mockMvc.perform(post("/bank/links/verify").with(BankTokens.walletRead())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountNumber":"123456789012","phone":"243810000002"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.verified").value(false));
	}

	@Test
	void rejectsAnInvalidAccountNumberBeforeCallingTheUseCase() throws Exception {
		mockMvc.perform(post("/bank/links/verify").with(BankTokens.walletRead())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountNumber":"12","phone":"243810000001"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.errors[?(@.field == 'accountNumber')]").exists());
		verify(links, never()).execute("12", "243810000001");
	}

	@Test
	void rejectsAMissingPhone() throws Exception {
		mockMvc.perform(post("/bank/links/verify").with(BankTokens.walletRead())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountNumber":"123456789012"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.errors[?(@.field == 'phone')]").exists());
	}

}
