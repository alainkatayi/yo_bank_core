package com.mywallet.bank.api;

import com.mywallet.bank.application.exception.AccountNotFoundException;
import com.mywallet.bank.application.port.in.GetAccountUseCase;
import com.mywallet.bank.application.result.AccountView;
import com.mywallet.bank.domain.exception.InvalidAccountNumberException;
import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankAccountController.class)
@WithBankSecurity
class BankAccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetAccountUseCase accounts;

	@Test
	void readsAnAccountWithTheMaskedHolder() throws Exception {
		when(accounts.execute("123456789012")).thenReturn(new AccountView(
				"123456789012",
				"A*** K***",
				Money.of("70000.00"),
				AccountStatus.ACTIVE
		));

		mockMvc.perform(get("/bank/accounts/123456789012").with(BankTokens.walletRead()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountNumber").value("123456789012"))
				.andExpect(jsonPath("$.holderName").value("A*** K***"))
				.andExpect(jsonPath("$.balance").value("70000.00"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void mapsAnUnknownAccount() throws Exception {
		when(accounts.execute("123456789012")).thenThrow(new AccountNotFoundException("123456789012"));

		mockMvc.perform(get("/bank/accounts/123456789012").with(BankTokens.walletRead()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"))
				.andExpect(jsonPath("$.detail").value("Compte introuvable"));
	}

	@Test
	void mapsAnInvalidAccountNumber() throws Exception {
		when(accounts.execute("abc")).thenThrow(new InvalidAccountNumberException("format secret-abc"));

		String body = mockMvc.perform(get("/bank/accounts/abc").with(BankTokens.walletRead()))
				.andExpect(status().is(422))
				.andExpect(jsonPath("$.code").value("INVALID_ACCOUNT_NUMBER"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertThat(body).doesNotContain("secret-abc");
	}

}
