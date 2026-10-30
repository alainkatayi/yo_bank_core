package com.mywallet.bank.api;

import com.jayway.jsonpath.JsonPath;
import com.mywallet.bank.application.service.AbstractApplicationIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class BankAdminApiIntegrationTest extends AbstractApplicationIntegrationTest {

	private static final AtomicInteger PHONES = new AtomicInteger(1);

	@Autowired
	private MockMvc mockMvc;

	@Test
	void searchesCustomersWithACappedPage() throws Exception {
		String marker = "zq" + UUID.randomUUID().toString().substring(0, 8);
		createCustomer("Ada", marker);
		createCustomer("Bea", marker);
		String newest = createCustomer("Clea", marker);

		String firstPage = mockMvc.perform(get("/bank/admin/customers")
						.with(BankTokens.admin())
						.param("search", marker.toUpperCase())
						.param("page", "0")
						.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(3))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(1))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].id").value(newest))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String secondPage = mockMvc.perform(get("/bank/admin/customers")
						.with(BankTokens.admin())
						.param("search", marker)
						.param("page", "1")
						.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].firstName").value("Bea"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertThat(JsonPath.<String>read(firstPage, "$.items[0].id"))
				.isNotEqualTo(JsonPath.<String>read(secondPage, "$.items[0].id"));

		mockMvc.perform(get("/bank/admin/customers")
						.with(BankTokens.auditor())
						.param("search", marker)
						.param("size", "500"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size").value(100))
				.andExpect(jsonPath("$.totalItems").value(3))
				.andExpect(jsonPath("$.items.length()").value(3));
	}

	@Test
	void listsAccountOperationsNewestFirstAndFiltersThem() throws Exception {
		Opened opened = openAccount("Ops", "Holder");
		deposit(opened.accountNumber(), "50.00");
		String debit = debit(opened.accountNumber(), "10.00");
		String credit = credit(opened.accountNumber(), "5.00");

		String history = mockMvc.perform(get("/bank/admin/accounts/" + opened.accountNumber() + "/operations")
						.with(BankTokens.auditor()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(3))
				.andExpect(jsonPath("$.items[0].reference").value(credit))
				.andExpect(jsonPath("$.items[0].type").value("CREDIT"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertThat(JsonPath.<String>read(history, "$.items[1].reference")).isEqualTo(debit);

		mockMvc.perform(get("/bank/admin/accounts/" + opened.accountNumber() + "/operations")
						.with(BankTokens.admin())
						.param("type", "DEBIT")
						.param("status", "SUCCESS"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(1))
				.andExpect(jsonPath("$.items[0].reference").value(debit))
				.andExpect(jsonPath("$.items[0].status").value("SUCCESS"));
	}

	@Test
	void blocksThenRejectsAWalletDebitUntilUnblocked() throws Exception {
		Opened opened = openAccount("Bloc", "Compte");
		deposit(opened.accountNumber(), "100.00");

		mockMvc.perform(post("/bank/admin/accounts/" + opened.accountNumber() + "/block").with(BankTokens.admin()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountNumber").value(opened.accountNumber()))
				.andExpect(jsonPath("$.status").value("BLOCKED"));
		assertAudit("ACCOUNT_BLOCKED", "ACCOUNT", opened.accountNumber(), null);

		mockMvc.perform(post("/bank/operations/debit").with(BankTokens.walletDebit())
						.contentType(MediaType.APPLICATION_JSON)
						.content(operation(opened.accountNumber(), "10.00")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.rejectionReason").value("ACCOUNT_BLOCKED"));

		mockMvc.perform(post("/bank/admin/accounts/" + opened.accountNumber() + "/unblock").with(BankTokens.admin()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"));
		assertAudit("ACCOUNT_UNBLOCKED", "ACCOUNT", opened.accountNumber(), null);

		mockMvc.perform(post("/bank/operations/debit").with(BankTokens.walletDebit())
						.contentType(MediaType.APPLICATION_JSON)
						.content(operation(opened.accountNumber(), "10.00")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.balanceAfter").value("90.00"));
	}

	@Test
	void closesOnlyAnEmptyAccountAndThenRejectsMovements() throws Exception {
		Opened opened = openAccount("Clos", "Compte");
		deposit(opened.accountNumber(), "40.00");

		mockMvc.perform(post("/bank/admin/accounts/" + opened.accountNumber() + "/close").with(BankTokens.admin()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ACCOUNT_NOT_EMPTY"));
		assertThat(auditCount("ACCOUNT_CLOSED", opened.accountNumber())).isZero();

		debit(opened.accountNumber(), "40.00");
		mockMvc.perform(post("/bank/admin/accounts/" + opened.accountNumber() + "/close").with(BankTokens.admin()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CLOSED"))
				.andExpect(jsonPath("$.balance").value("0.00"));
		assertAudit("ACCOUNT_CLOSED", "ACCOUNT", opened.accountNumber(), null);

		mockMvc.perform(post("/bank/operations/credit").with(BankTokens.walletCredit())
						.contentType(MediaType.APPLICATION_JSON)
						.content(operation(opened.accountNumber(), "1.00")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.rejectionReason").value("ACCOUNT_CLOSED"));

		mockMvc.perform(post("/bank/admin/accounts/" + opened.accountNumber() + "/unblock").with(BankTokens.admin()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVALID_STATE_TRANSITION"));
		assertThat(auditCount("ACCOUNT_UNBLOCKED", opened.accountNumber())).isZero();
	}

	@Test
	void refusesToOpenAnAccountForASuspendedCustomer() throws Exception {
		String customerId = createCustomer("Susp", "Client");
		mockMvc.perform(post("/bank/admin/customers/" + customerId + "/suspend").with(BankTokens.admin()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUSPENDED"));
		assertAudit("CUSTOMER_SUSPENDED", "CUSTOMER", customerId, null);

		mockMvc.perform(post("/bank/admin/customers/" + customerId + "/accounts").with(BankTokens.admin()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CUSTOMER_SUSPENDED"));
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from admin_audit_log where action = 'ACCOUNT_OPENED' and details = ?",
				Integer.class,
				"customerId=" + customerId
		)).isZero();

		mockMvc.perform(post("/bank/admin/customers/" + customerId + "/reactivate").with(BankTokens.auditor()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
		assertThat(auditCount("CUSTOMER_REACTIVATED", customerId)).isZero();

		mockMvc.perform(get("/bank/admin/audit").with(BankTokens.auditor()).param("action", "CUSTOMER_SUSPENDED"))
				.andExpect(status().isOk());
	}

	@Test
	void writesOneAuditRowPerSuccessfulAdminActionWithTheJwtActor() throws Exception {
		String customerId = createCustomer("Audit", "Trace");
		assertAudit("CUSTOMER_CREATED", "CUSTOMER", customerId, null);

		String accountNumber = openAccount(customerId);
		assertThat(auditCount("ACCOUNT_OPENED", accountNumber)).isEqualTo(1);
		assertThat(details("ACCOUNT_OPENED", accountNumber)).isEqualTo("customerId=" + customerId);

		String deposit = deposit(accountNumber, "15.00");
		assertThat(details("ACCOUNT_DEPOSIT", accountNumber)).isEqualTo("amount=15.00;reference=" + deposit);

		String body = mockMvc.perform(get("/bank/admin/audit")
						.with(BankTokens.auditor())
						.param("actor", BankTokens.ADMIN_ID)
						.param("size", "20"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		List<String> targets = JsonPath.read(body, "$.items[*].targetId");
		assertThat(targets.indexOf(accountNumber)).isLessThan(targets.indexOf(customerId));
		assertThat(JsonPath.<String>read(body, "$.items[0].actorUsername")).isEqualTo("admin1");
	}

	private void assertAudit(String action, String targetType, String targetId, String details) {
		assertThat(auditCount(action, targetId)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"select actor_id from admin_audit_log where action = ? and target_id = ?",
				String.class,
				action,
				targetId
		)).isEqualTo(BankTokens.ADMIN_ID);
		assertThat(jdbcTemplate.queryForObject(
				"select actor_username from admin_audit_log where action = ? and target_id = ?",
				String.class,
				action,
				targetId
		)).isEqualTo("admin1");
		assertThat(jdbcTemplate.queryForObject(
				"select target_type from admin_audit_log where action = ? and target_id = ?",
				String.class,
				action,
				targetId
		)).isEqualTo(targetType);
		if (details != null) {
			assertThat(details(action, targetId)).isEqualTo(details);
		}
	}

	private int auditCount(String action, String targetId) {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from admin_audit_log where action = ? and target_id = ?",
				Integer.class,
				action,
				targetId
		);
		return count == null ? 0 : count;
	}

	private String details(String action, String targetId) {
		return jdbcTemplate.queryForObject(
				"select details from admin_audit_log where action = ? and target_id = ?",
				String.class,
				action,
				targetId
		);
	}

	private Opened openAccount(String firstName, String lastName) throws Exception {
		String customerId = createCustomer(firstName, lastName);
		return new Opened(customerId, openAccount(customerId));
	}

	private String openAccount(String customerId) throws Exception {
		MvcResult created = mockMvc.perform(post("/bank/admin/customers/" + customerId + "/accounts").with(BankTokens.admin()))
				.andExpect(status().isCreated())
				.andReturn();
		return JsonPath.read(created.getResponse().getContentAsString(), "$.accountNumber");
	}

	private String createCustomer(String firstName, String lastName) throws Exception {
		String phone = "245" + String.format("%09d", PHONES.incrementAndGet());
		MvcResult created = mockMvc.perform(post("/bank/admin/customers").with(BankTokens.admin())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName":"%s","lastName":"%s","phone":"%s"}
								""".formatted(firstName, lastName, phone)))
				.andExpect(status().isCreated())
				.andReturn();
		return JsonPath.read(created.getResponse().getContentAsString(), "$.id");
	}

	private String deposit(String accountNumber, String amount) throws Exception {
		String body = mockMvc.perform(post("/bank/admin/accounts/" + accountNumber + "/deposit").with(BankTokens.admin())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"amount":"%s"}
								""".formatted(amount)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		return JsonPath.read(body, "$.reference");
	}

	private String debit(String accountNumber, String amount) throws Exception {
		String body = mockMvc.perform(post("/bank/operations/debit").with(BankTokens.walletDebit())
						.contentType(MediaType.APPLICATION_JSON)
						.content(operation(accountNumber, amount)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		return JsonPath.read(body, "$.reference");
	}

	private String credit(String accountNumber, String amount) throws Exception {
		String body = mockMvc.perform(post("/bank/operations/credit").with(BankTokens.walletCredit())
						.contentType(MediaType.APPLICATION_JSON)
						.content(operation(accountNumber, amount)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		return JsonPath.read(body, "$.reference");
	}

	private static String operation(String accountNumber, String amount) {
		return """
				{"reference":"%s","accountNumber":"%s","amount":"%s"}
				""".formatted(UUID.randomUUID(), accountNumber, amount);
	}

	private record Opened(String customerId, String accountNumber) {
	}

}
