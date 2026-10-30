package com.mywallet.bank.api;

import com.mywallet.bank.application.command.OperationCommand;
import com.mywallet.bank.application.exception.ConcurrentUpdateException;
import com.mywallet.bank.application.exception.IdempotencyConflictException;
import com.mywallet.bank.application.exception.OperationNotFoundException;
import com.mywallet.bank.application.port.in.CreditAccountUseCase;
import com.mywallet.bank.application.port.in.DebitAccountUseCase;
import com.mywallet.bank.application.port.in.GetOperationUseCase;
import com.mywallet.bank.application.result.OperationResult;
import com.mywallet.bank.domain.exception.InvalidAmountException;
import com.mywallet.bank.domain.model.Money;
import com.mywallet.bank.domain.model.OperationStatus;
import com.mywallet.bank.domain.model.OperationType;
import com.mywallet.bank.domain.model.RejectionReason;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankOperationController.class)
class BankOperationControllerTest {

	private static final Instant CREATED_AT = Instant.parse("2026-10-29T12:00:00Z");
	private static final String ACCOUNT = "123456789012";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DebitAccountUseCase debits;

	@MockitoBean
	private CreditAccountUseCase credits;

	@MockitoBean
	private GetOperationUseCase operations;

	@Test
	void debitsAndReturnsTheSuccessBody() throws Exception {
		when(debits.execute(new OperationCommand("ref-debit", ACCOUNT, "3000.00")))
				.thenReturn(success("ref-debit", OperationType.DEBIT, "47000.00"));

		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson("ref-debit", ACCOUNT, "3000.00")))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.reference").value("ref-debit"))
				.andExpect(jsonPath("$.type").value("DEBIT"))
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.balanceAfter").value("47000.00"))
				.andExpect(jsonPath("$.createdAt").value("2026-10-29T12:00:00Z"))
				.andExpect(jsonPath("$.rejectionReason").doesNotExist());
	}

	@Test
	void creditsAndReturnsTheSuccessBody() throws Exception {
		when(credits.execute(new OperationCommand("ref-credit", ACCOUNT, "5000.00")))
				.thenReturn(success("ref-credit", OperationType.CREDIT, "5000.00"));

		mockMvc.perform(post("/bank/operations/credit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson("ref-credit", ACCOUNT, "5000.00")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.type").value("CREDIT"))
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.balanceAfter").value("5000.00"))
				.andExpect(jsonPath("$.rejectionReason").doesNotExist());
	}

	@Test
	void returnsARejectedDebitAsOk() throws Exception {
		when(debits.execute(any())).thenReturn(new OperationResult(
				"ref-rejected",
				OperationType.DEBIT,
				OperationStatus.REJECTED,
				RejectionReason.INSUFFICIENT_FUNDS,
				null,
				CREATED_AT
		));

		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson("ref-rejected", ACCOUNT, "10.00")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.rejectionReason").value("INSUFFICIENT_FUNDS"))
				.andExpect(jsonPath("$.createdAt").value("2026-10-29T12:00:00Z"))
				.andExpect(jsonPath("$.balanceAfter").doesNotExist());
	}

	@Test
	void readsAnOperation() throws Exception {
		when(operations.execute("ref-debit")).thenReturn(success("ref-debit", OperationType.DEBIT, "47000.00"));

		mockMvc.perform(get("/bank/operations/ref-debit"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reference").value("ref-debit"))
				.andExpect(jsonPath("$.status").value("SUCCESS"))
				.andExpect(jsonPath("$.balanceAfter").value("47000.00"));
	}

	@Test
	void rejectsAMissingReference() throws Exception {
		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountNumber":"123456789012","amount":"10.00"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.errors[?(@.field == 'reference')]").exists());
		verify(debits, never()).execute(any());
	}

	@Test
	void rejectsAReferenceLongerThan64Characters() throws Exception {
		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson("r".repeat(65), ACCOUNT, "10.00")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.errors[?(@.field == 'reference')]").exists());
	}

	@Test
	void rejectsAnAmountSentAsAJsonNumber() throws Exception {
		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"reference":"ref-1","accountNumber":"123456789012","amount":5000.5}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
				.andExpect(jsonPath("$.detail").value("La requête est illisible"));
		verify(debits, never()).execute(any());
	}

	@Test
	void rejectsUnreadableJson() throws Exception {
		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
		verify(debits, never()).execute(any());
	}

	@Test
	void mapsAnInvalidAmount() throws Exception {
		when(debits.execute(any())).thenThrow(new InvalidAmountException("échelle 12.345"));

		String body = problem(mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson("ref-1", ACCOUNT, "12.345")))
				.andExpect(status().is(422))
				.andExpect(jsonPath("$.code").value("INVALID_AMOUNT"))
				.andReturn());
		assertThat(body).doesNotContain("12.345");
	}

	@Test
	void mapsAnUnknownOperation() throws Exception {
		when(operations.execute("missing")).thenThrow(new OperationNotFoundException("missing"));

		mockMvc.perform(get("/bank/operations/missing"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("OPERATION_NOT_FOUND"))
				.andExpect(jsonPath("$.detail").value("Opération introuvable"));
	}

	@Test
	void mapsAnIdempotencyConflict() throws Exception {
		when(debits.execute(any())).thenThrow(new IdempotencyConflictException("ref-1"));

		mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson("ref-1", ACCOUNT, "10.00")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("IDEMPOTENCY_CONFLICT"));
	}

	@Test
	void mapsAConcurrentUpdateWithRetryAfter() throws Exception {
		when(debits.execute(any())).thenThrow(new ConcurrentUpdateException("ref-1", new RuntimeException("lock-detail")));

		String body = problem(mockMvc.perform(post("/bank/operations/debit")
						.contentType(MediaType.APPLICATION_JSON)
						.content(operationJson("ref-1", ACCOUNT, "10.00")))
				.andExpect(status().isServiceUnavailable())
				.andExpect(header().string("Retry-After", "1"))
				.andExpect(jsonPath("$.code").value("CONCURRENT_UPDATE"))
				.andReturn());
		assertThat(body).doesNotContain("lock-detail");
	}

	@Test
	void hidesUnexpectedFailures() throws Exception {
		when(debits.execute(any())).thenThrow(new LeakProbeException());
		Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		try {
			String body = problem(mockMvc.perform(post("/bank/operations/debit")
							.contentType(MediaType.APPLICATION_JSON)
							.content(operationJson("ref-1", ACCOUNT, "10.00")))
					.andExpect(status().isInternalServerError())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
					.andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
					.andExpect(jsonPath("$.detail").value("Une erreur interne est survenue"))
					.andReturn());
			assertThat(body).doesNotContain(
					"LeakProbeException",
					"PSQLException",
					"243811199991",
					"123456789012",
					"stackTrace",
					"\"trace\""
			);
			assertThat(appender.list).anyMatch(event ->
					event.getLevel().toString().equals("ERROR")
							&& event.getFormattedMessage().contains("LeakProbeException")
							&& !event.getFormattedMessage().contains("243811199991")
							&& !event.getFormattedMessage().contains("123456789012")
			);
		} finally {
			logger.detachAppender(appender);
		}
	}

	private static OperationResult success(String reference, OperationType type, String balance) {
		return new OperationResult(reference, type, OperationStatus.SUCCESS, null, Money.of(balance), CREATED_AT);
	}

	private static String operationJson(String reference, String accountNumber, String amount) {
		return """
				{"reference":"%s","accountNumber":"%s","amount":"%s"}
				""".formatted(reference, accountNumber, amount);
	}

	private static String problem(MvcResult result) throws Exception {
		return result.getResponse().getContentAsString();
	}

	private static final class LeakProbeException extends RuntimeException {

		private LeakProbeException() {
			super("org.postgresql.util.PSQLException detail phone=243811199991 account=123456789012");
		}

	}

}
