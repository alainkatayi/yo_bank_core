package com.mywallet.bank.api;

import com.mywallet.bank.application.port.in.ConfirmLinkUseCase;
import com.mywallet.bank.application.port.in.InitiateLinkUseCase;
import com.mywallet.bank.application.result.ConfirmLinkResult;
import com.mywallet.bank.application.result.InitiateLinkResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
	private InitiateLinkUseCase initiations;

	@MockitoBean
	private ConfirmLinkUseCase confirmations;

	@Test
	void acceptsAnInitiation() throws Exception {
		when(initiations.execute(any(), any())).thenReturn(new InitiateLinkResult(
				UUID.fromString("33333333-3333-3333-3333-333333333333"),
				300
		));

		mockMvc.perform(post("/bank/links/initiate").with(BankTokens.walletLink())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountNumber":"123456789012","phone":"243810000001"}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.linkId").value("33333333-3333-3333-3333-333333333333"))
				.andExpect(jsonPath("$.expiresIn").value(300));
	}

	@Test
	void confirmsALink() throws Exception {
		when(confirmations.execute(any(), any())).thenReturn(new ConfirmLinkResult("123456789012"));

		mockMvc.perform(post("/bank/links/confirm").with(BankTokens.walletLink())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"linkId":"33333333-3333-3333-3333-333333333333","code":"123456"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.verified").value(true))
				.andExpect(jsonPath("$.accountNumber").value("123456789012"));
	}

	@Test
	void rejectsAnInvalidAccountNumberBeforeCallingTheUseCase() throws Exception {
		mockMvc.perform(post("/bank/links/initiate").with(BankTokens.walletLink())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountNumber":"12","phone":"243810000001"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.errors[?(@.field == 'accountNumber')]").exists());
		verify(initiations, never()).execute(any(), any());
	}

	@Test
	void rejectsAMissingPhone() throws Exception {
		mockMvc.perform(post("/bank/links/initiate").with(BankTokens.walletLink())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountNumber":"123456789012"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.errors[?(@.field == 'phone')]").exists());
	}

}
