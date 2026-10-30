package com.mywallet.bank.api;

import com.jayway.jsonpath.JsonPath;
import com.mywallet.bank.application.service.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class BankApiIntegrationTest extends AbstractApplicationIntegrationTest {

	private static final AtomicInteger PHONES = new AtomicInteger(1_000_000);

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createsACustomerThenDepositsAndDebits() throws Exception {
		Opened opened = openAccount();
		String deposit = mockMvc.perform(post("/bank/admin/accounts/" + opened.accountNumber() + "/deposit")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":"100000.00"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.type").value("CREDIT"))
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.balanceAfter").value("100000.00"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String depositReference = JsonPath.read(deposit, "$.reference");
		assertThat(depositReference).startsWith("ADMIN-DEP-");
		assertThat(depositReference).hasSize("ADMIN-DEP-".length() + 36);

		String reference = "debit-" + UUID.randomUUID();
		String debit = postOperation("/bank/operations/debit", reference, opened.accountNumber(), "30000.00");

		mockMvc.perform(get("/bank/accounts/" + opened.accountNumber()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountNumber").value(opened.accountNumber()))
				.andExpect(jsonPath("$.holderName").value("A*** K***"))
				.andExpect(jsonPath("$.balance").value("70000.00"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));

		String read = mockMvc.perform(get("/bank/operations/" + reference))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertThat(read).isEqualTo(debit);
		assertThat(JsonPath.<String>read(debit, "$.balanceAfter")).isEqualTo("70000.00");
		assertThat(countByReference(reference)).isEqualTo(1);
	}

	@Test
	void rejectsADebitAboveTheBalanceWithoutChangingIt() throws Exception {
		Opened opened = openAccount();
		String reference = "overdraft-" + UUID.randomUUID();

		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson(reference, opened.accountNumber(), "10.00")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.rejectionReason").value("INSUFFICIENT_FUNDS"))
				.andExpect(jsonPath("$.balanceAfter").doesNotExist());

		mockMvc.perform(get("/bank/accounts/" + opened.accountNumber()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.balance").value("0.00"));
	}

	@Test
	void replaysTheSameReferenceWithTheSameBody() throws Exception {
		Opened opened = fund(openAccount(), "1000.00");
		String reference = "replay-" + UUID.randomUUID();
		String body = operationJson(reference, opened.accountNumber(), "100.00");

		String first = mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String second = mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		assertThat(second).isEqualTo(first);
		assertThat(countByReference(reference)).isEqualTo(1);
		mockMvc.perform(get("/bank/accounts/" + opened.accountNumber()))
				.andExpect(jsonPath("$.balance").value("900.00"));
	}

	@Test
	void rejectsTheSameReferenceWhenTheAmountDiffers() throws Exception {
		Opened opened = fund(openAccount(), "1000.00");
		String reference = "conflict-" + UUID.randomUUID();
		postOperation("/bank/operations/debit", reference, opened.accountNumber(), "100.00");

		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson(reference, opened.accountNumber(), "200.00")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("IDEMPOTENCY_CONFLICT"));

		assertThat(countByReference(reference)).isEqualTo(1);
		mockMvc.perform(get("/bank/accounts/" + opened.accountNumber()))
				.andExpect(jsonPath("$.balance").value("900.00"));
	}

	@Test
	void persistsNothingWhenTheAccountIsUnknown() throws Exception {
		String reference = "missing-" + UUID.randomUUID();
		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson(reference, "999999999999", "10.00")))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("ACCOUNT_NOT_FOUND"));
		assertThat(countByReference(reference)).isEqualTo(0);
	}

	@Test
	void verifiesALinkOnlyForTheMatchingPhone() throws Exception {
		Opened opened = openAccount();
		String match = verify(opened.accountNumber(), opened.phone());
		String wrongPhone = verify(opened.accountNumber(), "244000000000");
		String unknownAccount = verify("000000000000", opened.phone());

		assertThat(JsonPath.<Boolean>read(match, "$.verified")).isTrue();
		assertThat(wrongPhone).isEqualTo(unknownAccount);
		assertThat(JsonPath.<Boolean>read(wrongPhone, "$.verified")).isFalse();
		assertThat(wrongPhone).doesNotContain(opened.phone(), opened.accountNumber(), "ACCOUNT_NOT_FOUND");
	}

	@Test
	void rejectsADuplicatePhone() throws Exception {
		String phone = nextPhone();
		createCustomer(phone);
		mockMvc.perform(post("/bank/admin/customers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(customerJson(phone)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("DUPLICATE_PHONE"));
	}

	private Opened fund(Opened opened, String amount) throws Exception {
		mockMvc.perform(post("/bank/admin/accounts/" + opened.accountNumber() + "/deposit")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":"%s"}
								""".formatted(amount)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"));
		return opened;
	}

	private Opened openAccount() throws Exception {
		String phone = nextPhone();
		String customerId = createCustomer(phone);
		MvcResult created = mockMvc.perform(post("/bank/admin/customers/" + customerId + "/accounts"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.balance").value("0.00"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.holderName").value("A*** K***"))
				.andReturn();
		String accountNumber = JsonPath.read(created.getResponse().getContentAsString(), "$.accountNumber");
		assertThat(accountNumber).matches("\\d{12}");
		assertThat(created.getResponse().getHeader("Location")).endsWith("/bank/accounts/" + accountNumber);
		return new Opened(customerId, accountNumber, phone);
	}

	private String createCustomer(String phone) throws Exception {
		MvcResult created = mockMvc.perform(post("/bank/admin/customers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(customerJson(phone)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andReturn();
		String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
		assertThat(created.getResponse().getHeader("Location")).contains(id);
		return id;
	}

	private String postOperation(String path, String reference, String accountNumber, String amount) throws Exception {
		return mockMvc.perform(post(path)
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson(reference, accountNumber, amount)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
	}

	private String verify(String accountNumber, String phone) throws Exception {
		return mockMvc.perform(post("/bank/links/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountNumber":"%s","phone":"%s"}
								""".formatted(accountNumber, phone)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
	}

	private static String customerJson(String phone) {
		return """
				{"firstName":"Amina","lastName":"Kabila","phone":"%s","email":"amina@example.com"}
				""".formatted(phone);
	}

	private static String operationJson(String reference, String accountNumber, String amount) {
		return """
				{"reference":"%s","accountNumber":"%s","amount":"%s"}
				""".formatted(reference, accountNumber, amount);
	}

	private static String nextPhone() {
		return "244" + String.format("%09d", PHONES.incrementAndGet());
	}

	private record Opened(String customerId, String accountNumber, String phone) {
	}

}
