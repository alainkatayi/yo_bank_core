package com.mywallet.bank.api;

import com.mywallet.bank.application.PageQuery;
import com.mywallet.bank.application.PageResult;
import com.mywallet.bank.application.port.in.BlockAccountUseCase;
import com.mywallet.bank.application.port.in.CloseAccountUseCase;
import com.mywallet.bank.application.port.in.CreateCustomerUseCase;
import com.mywallet.bank.application.port.in.CreditAccountUseCase;
import com.mywallet.bank.application.port.in.DebitAccountUseCase;
import com.mywallet.bank.application.port.in.DepositFundsUseCase;
import com.mywallet.bank.application.port.in.GetAccountUseCase;
import com.mywallet.bank.application.port.in.GetCustomerUseCase;
import com.mywallet.bank.application.port.in.GetOperationUseCase;
import com.mywallet.bank.application.port.in.ListAccountOperationsUseCase;
import com.mywallet.bank.application.port.in.ListCustomerAccountsUseCase;
import com.mywallet.bank.application.port.in.OpenAccountUseCase;
import com.mywallet.bank.application.port.in.ReactivateCustomerUseCase;
import com.mywallet.bank.application.port.in.SearchAuditLogUseCase;
import com.mywallet.bank.application.port.in.SearchCustomersUseCase;
import com.mywallet.bank.application.port.in.SuspendCustomerUseCase;
import com.mywallet.bank.application.port.in.UnblockAccountUseCase;
import com.mywallet.bank.application.port.in.VerifyLinkUseCase;
import com.mywallet.bank.application.result.AccountView;
import com.mywallet.bank.application.result.AdminAccountView;
import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.model.AccountStatus;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
		BankOperationController.class,
		BankAccountController.class,
		BankLinkController.class,
		BankAdminController.class,
		BankAdminQueryController.class
})
@WithBankSecurity
class BankAuthorizationTest {

	private static final UUID CUSTOMER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final Instant CREATED_AT = Instant.parse("2026-10-30T08:00:00Z");
	private static final String DEBIT_BODY = """
			{"reference":"ref-1","accountNumber":"123456789012","amount":"10.00"}
			""";
	private static final String CUSTOMER_BODY = """
			{"firstName":"Amina","lastName":"Kabila","phone":"243810000001"}
			""";
	private static final String DEPOSIT_BODY = """
			{"amount":"10.00"}
			""";
	private static final String VERIFY_BODY = """
			{"accountNumber":"123456789012","phone":"243810000001"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DebitAccountUseCase debits;

	@MockitoBean
	private CreditAccountUseCase credits;

	@MockitoBean
	private GetOperationUseCase operations;

	@MockitoBean
	private GetAccountUseCase accounts;

	@MockitoBean
	private VerifyLinkUseCase links;

	@MockitoBean
	private CreateCustomerUseCase customers;

	@MockitoBean
	private OpenAccountUseCase openings;

	@MockitoBean
	private DepositFundsUseCase deposits;

	@MockitoBean
	private BlockAccountUseCase blocks;

	@MockitoBean
	private UnblockAccountUseCase unblocks;

	@MockitoBean
	private CloseAccountUseCase closures;

	@MockitoBean
	private SuspendCustomerUseCase suspensions;

	@MockitoBean
	private ReactivateCustomerUseCase reactivations;

	@MockitoBean
	private SearchCustomersUseCase customerSearch;

	@MockitoBean
	private GetCustomerUseCase customer;

	@MockitoBean
	private ListCustomerAccountsUseCase customerAccounts;

	@MockitoBean
	private ListAccountOperationsUseCase accountOperations;

	@MockitoBean
	private SearchAuditLogUseCase audit;

	@BeforeEach
	void stubAuthorizedUseCases() {
		OperationResult success = new OperationResult(
				"ref-1",
				OperationType.DEBIT,
				OperationStatus.SUCCESS,
				null,
				Money.of("10.00"),
				CREATED_AT
		);
		CustomerView customerView = new CustomerView(
				CUSTOMER_ID,
				"Amina",
				"Kabila",
				"243810000001",
				null,
				"ACTIVE",
				CREATED_AT
		);
		AccountView accountView = new AccountView("123456789012", "A*** K***", Money.of("10.00"), AccountStatus.ACTIVE);
		AdminAccountView adminAccount = new AdminAccountView("123456789012", Money.of("10.00"), AccountStatus.ACTIVE);
		lenient().when(debits.execute(any())).thenReturn(success);
		lenient().when(credits.execute(any())).thenReturn(success);
		lenient().when(operations.execute(any())).thenReturn(success);
		lenient().when(accounts.execute(any())).thenReturn(accountView);
		lenient().when(links.execute(any(), any())).thenReturn(true);
		lenient().when(customers.execute(any())).thenReturn(customerView);
		lenient().when(openings.execute(any())).thenReturn(accountView);
		lenient().when(deposits.execute(any())).thenReturn(success);
		lenient().when(blocks.execute(any())).thenReturn(adminAccount);
		lenient().when(unblocks.execute(any())).thenReturn(adminAccount);
		lenient().when(closures.execute(any())).thenReturn(adminAccount);
		lenient().when(suspensions.execute(any())).thenReturn(customerView);
		lenient().when(reactivations.execute(any())).thenReturn(customerView);
		lenient().when(customerSearch.execute(nullable(String.class), any(PageQuery.class)))
				.thenReturn(new PageResult<>(List.of(), 0, 20, 0));
		lenient().when(customer.execute(any())).thenReturn(customerView);
		lenient().when(customerAccounts.execute(any())).thenReturn(List.of(adminAccount));
		lenient().when(accountOperations.execute(any(), nullable(OperationType.class), nullable(OperationStatus.class), any()))
				.thenReturn(new PageResult<>(List.of(), 0, 20, 0));
		lenient().when(audit.execute(nullable(String.class), nullable(AuditAction.class), any()))
				.thenReturn(new PageResult<>(List.of(), 0, 20, 0));
	}

	@ParameterizedTest(name = "{0} / {1}")
	@MethodSource("routes")
	void enforcesTheCallerMatrix(String name, HttpMethod method, String path, String body, Caller caller, int expected)
			throws Exception {
		MockHttpServletRequestBuilder request = request(method, path);
		if (body != null) {
			request.contentType(MediaType.APPLICATION_JSON).content(body);
		}
		request.with(caller.token());
		var result = mockMvc.perform(request).andExpect(status().is(expected));
		if (expected == 401) {
			result.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
					.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
					.andExpect(jsonPath("$.detail").value("Authentification requise"));
		}
		if (expected == 403) {
			result.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
					.andExpect(jsonPath("$.code").value("FORBIDDEN"))
					.andExpect(jsonPath("$.detail").value("Accès refusé"));
		}
	}

	static Stream<Arguments> routes() {
		String customer = "/bank/admin/customers/" + CUSTOMER_ID;
		String account = "/bank/admin/accounts/123456789012";
		return Stream.of(
				route("débit", HttpMethod.POST, "/bank/operations/debit", DEBIT_BODY, 401, 200, 403, 403, 403),
				route("crédit", HttpMethod.POST, "/bank/operations/credit", DEBIT_BODY, 401, 200, 403, 403, 403),
				route("lecture opération", HttpMethod.GET, "/bank/operations/ref-1", null, 401, 200, 200, 403, 403),
				route("lecture compte", HttpMethod.GET, "/bank/accounts/123456789012", null, 401, 200, 200, 403, 403),
				route("vérification liaison", HttpMethod.POST, "/bank/links/verify", VERIFY_BODY, 401, 200, 200, 403, 403),
				route("recherche clients", HttpMethod.GET, "/bank/admin/customers", null, 401, 403, 403, 200, 200),
				route("détail client", HttpMethod.GET, customer, null, 401, 403, 403, 200, 200),
				route("comptes client", HttpMethod.GET, customer + "/accounts", null, 401, 403, 403, 200, 200),
				route("historique", HttpMethod.GET, account + "/operations", null, 401, 403, 403, 200, 200),
				route("audit", HttpMethod.GET, "/bank/admin/audit", null, 401, 403, 403, 200, 200),
				route("création client", HttpMethod.POST, "/bank/admin/customers", CUSTOMER_BODY, 401, 403, 403, 201, 403),
				route("ouverture compte", HttpMethod.POST, customer + "/accounts", null, 401, 403, 403, 201, 403),
				route("dépôt", HttpMethod.POST, account + "/deposit", DEPOSIT_BODY, 401, 403, 403, 200, 403),
				route("blocage", HttpMethod.POST, account + "/block", null, 401, 403, 403, 200, 403),
				route("déblocage", HttpMethod.POST, account + "/unblock", null, 401, 403, 403, 200, 403),
				route("clôture", HttpMethod.POST, account + "/close", null, 401, 403, 403, 200, 403),
				route("suspension", HttpMethod.POST, customer + "/suspend", null, 401, 403, 403, 200, 403),
				route("réactivation", HttpMethod.POST, customer + "/reactivate", null, 401, 403, 403, 200, 403)
		).flatMap(route -> route);
	}

	private static Stream<Arguments> route(
			String name,
			HttpMethod method,
			String path,
			String body,
			int anonymous,
			int walletAll,
			int walletRead,
			int admin,
			int auditor
	) {
		return Stream.of(
				Arguments.of(name, method, path, body, Caller.ANONYMOUS, anonymous),
				Arguments.of(name, method, path, body, Caller.WALLET_ALL, walletAll),
				Arguments.of(name, method, path, body, Caller.WALLET_READ, walletRead),
				Arguments.of(name, method, path, body, Caller.ADMIN, admin),
				Arguments.of(name, method, path, body, Caller.AUDITOR, auditor)
		);
	}

	enum Caller {
		ANONYMOUS,
		WALLET_ALL,
		WALLET_READ,
		ADMIN,
		AUDITOR;

		org.springframework.test.web.servlet.request.RequestPostProcessor token() {
			return switch (this) {
				case ANONYMOUS -> request -> request;
				case WALLET_ALL -> BankTokens.walletAll();
				case WALLET_READ -> BankTokens.walletRead();
				case ADMIN -> BankTokens.admin();
				case AUDITOR -> BankTokens.auditor();
			};
		}
	}

}
