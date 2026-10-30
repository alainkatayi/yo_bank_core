package com.mywallet.bank.api;

import com.mywallet.bank.application.command.CreateCustomerCommand;
import com.mywallet.bank.application.exception.CustomerNotFoundException;
import com.mywallet.bank.application.exception.DuplicatePhoneException;
import com.mywallet.bank.application.port.in.CreateCustomerUseCase;
import com.mywallet.bank.application.port.in.DepositFundsUseCase;
import com.mywallet.bank.application.port.in.OpenAccountUseCase;
import com.mywallet.bank.application.result.AccountView;
import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.exception.InvalidAmountException;
import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankAdminController.class)
class BankAdminControllerTest {

	private static final UUID CUSTOMER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final Instant CREATED_AT = Instant.parse("2026-10-29T12:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CreateCustomerUseCase customers;

	@MockitoBean
	private OpenAccountUseCase accounts;

	@MockitoBean
	private DepositFundsUseCase deposits;

	@Test
	void createsACustomer() throws Exception {
		when(customers.execute(new CreateCustomerCommand(
				"Amina",
				"Kabila",
				"243810000001",
				"amina@example.com"
		))).thenReturn(new CustomerView(
				CUSTOMER_ID,
				"Amina",
				"Kabila",
				"243810000001",
				"amina@example.com",
				"ACTIVE",
				CREATED_AT
		));

		mockMvc.perform(post("/bank/admin/customers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName":"Amina","lastName":"Kabila","phone":"243810000001","email":"amina@example.com"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.containsString(CUSTOMER_ID.toString())))
				.andExpect(jsonPath("$.id").value(CUSTOMER_ID.toString()))
				.andExpect(jsonPath("$.firstName").value("Amina"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.phone").value("243810000001"));
	}

	@Test
	void opensAnAccount() throws Exception {
		when(accounts.execute(CUSTOMER_ID)).thenReturn(new AccountView(
				"123456789012",
				"A*** K***",
				Money.zero(),
				AccountStatus.ACTIVE
		));

		mockMvc.perform(post("/bank/admin/customers/" + CUSTOMER_ID + "/accounts"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/bank/accounts/123456789012")))
				.andExpect(jsonPath("$.accountNumber").value("123456789012"))
				.andExpect(jsonPath("$.holderName").value("A*** K***"))
				.andExpect(jsonPath("$.balance").value("0.00"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void depositsWithTheOperationBody() throws Exception {
		when(deposits.execute("123456789012", "100000.00")).thenReturn(new OperationResult(
				"ADMIN-DEP-11111111-1111-1111-1111-111111111111",
				OperationType.CREDIT,
				OperationStatus.SUCCESS,
				null,
				Money.of("100000.00"),
				CREATED_AT
		));

		mockMvc.perform(post("/bank/admin/accounts/123456789012/deposit")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":"100000.00"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reference").value("ADMIN-DEP-11111111-1111-1111-1111-111111111111"))
				.andExpect(jsonPath("$.type").value("CREDIT"))
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.balanceAfter").value("100000.00"));
	}

	@Test
	void mapsADuplicatePhone() throws Exception {
		when(customers.execute(any())).thenThrow(new DuplicatePhoneException());

		String body = mockMvc.perform(post("/bank/admin/customers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName":"Amina","lastName":"Kabila","phone":"243810000001"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_PHONE"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertThat(body).doesNotContain("243810000001");
	}

	@Test
	void mapsAnUnknownCustomer() throws Exception {
		when(accounts.execute(CUSTOMER_ID)).thenThrow(new CustomerNotFoundException());

		mockMvc.perform(post("/bank/admin/customers/" + CUSTOMER_ID + "/accounts"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
	}

	@Test
	void rejectsACustomerIdThatIsNotAUuid() throws Exception {
		mockMvc.perform(post("/bank/admin/customers/not-a-uuid/accounts"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
		verify(accounts, never()).execute(any());
	}

	@Test
	void rejectsADepositAmountSentAsAJsonNumber() throws Exception {
		mockMvc.perform(post("/bank/admin/accounts/123456789012/deposit")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":100000.5}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
		verify(deposits, never()).execute(any(), any());
	}

	@Test
	void mapsAnInvalidDepositAmount() throws Exception {
		when(deposits.execute(any(), any())).thenThrow(new InvalidAmountException("négatif -1"));

		String body = mockMvc.perform(post("/bank/admin/accounts/123456789012/deposit")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":"-1.00"}
								"""))
				.andExpect(status().is(422))
				.andExpect(jsonPath("$.code").value("INVALID_AMOUNT"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertThat(body).doesNotContain("-1");
	}

}
