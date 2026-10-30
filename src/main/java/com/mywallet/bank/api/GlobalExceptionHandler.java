package com.mywallet.bank.api;

import com.mywallet.bank.application.exception.AccountNotFoundException;
import com.mywallet.bank.application.exception.ConcurrentUpdateException;
import com.mywallet.bank.application.exception.CustomerNotFoundException;
import com.mywallet.bank.application.exception.CustomerSuspendedException;
import com.mywallet.bank.application.exception.DuplicatePhoneException;
import com.mywallet.bank.application.exception.DuplicateUsernameException;
import com.mywallet.bank.application.exception.InvalidCredentialsException;
import com.mywallet.bank.application.exception.IdempotencyConflictException;
import com.mywallet.bank.application.exception.InvalidQueryException;
import com.mywallet.bank.application.exception.OperationNotFoundException;
import com.mywallet.bank.domain.exception.AccountNotEmptyException;
import com.mywallet.bank.domain.exception.InvalidAccountNumberException;
import com.mywallet.bank.domain.exception.InvalidAmountException;
import com.mywallet.bank.domain.exception.InvalidStateTransitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		List<Map<String, String>> errors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> {
					Map<String, String> field = new LinkedHashMap<>();
					field.put("field", error.getField());
					field.put("message", safeMessage(error.getDefaultMessage(), error.getRejectedValue()));
					return field;
				})
				.toList();
		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La requête est invalide", request);
		problem.setProperty("errors", errors);
		return body(HttpStatus.BAD_REQUEST, problem);
	}

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	ResponseEntity<ProblemDetail> malformed(Exception exception, HttpServletRequest request) {
		log.warn("Malformed API request type={}", exception.getClass().getSimpleName());
		return body(
				HttpStatus.BAD_REQUEST,
				problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "La requête est illisible", request)
		);
	}

	@ExceptionHandler(InvalidAmountException.class)
	ResponseEntity<ProblemDetail> invalidAmount(HttpServletRequest request) {
		return body(
				HttpStatus.UNPROCESSABLE_CONTENT,
				problem(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_AMOUNT", "Le montant est invalide", request)
		);
	}

	@ExceptionHandler(InvalidAccountNumberException.class)
	ResponseEntity<ProblemDetail> invalidAccountNumber(HttpServletRequest request) {
		return body(
				HttpStatus.UNPROCESSABLE_CONTENT,
				problem(
						HttpStatus.UNPROCESSABLE_CONTENT,
						"INVALID_ACCOUNT_NUMBER",
						"Le numéro de compte est invalide",
						request
				)
		);
	}

	@ExceptionHandler(AccountNotFoundException.class)
	ResponseEntity<ProblemDetail> accountNotFound(HttpServletRequest request) {
		return body(
				HttpStatus.NOT_FOUND,
				problem(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "Compte introuvable", request)
		);
	}

	@ExceptionHandler(OperationNotFoundException.class)
	ResponseEntity<ProblemDetail> operationNotFound(HttpServletRequest request) {
		return body(
				HttpStatus.NOT_FOUND,
				problem(HttpStatus.NOT_FOUND, "OPERATION_NOT_FOUND", "Opération introuvable", request)
		);
	}

	@ExceptionHandler(CustomerNotFoundException.class)
	ResponseEntity<ProblemDetail> customerNotFound(HttpServletRequest request) {
		return body(
				HttpStatus.NOT_FOUND,
				problem(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Client introuvable", request)
		);
	}

	@ExceptionHandler(IdempotencyConflictException.class)
	ResponseEntity<ProblemDetail> idempotencyConflict(HttpServletRequest request) {
		return body(
				HttpStatus.CONFLICT,
				problem(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", "La référence est déjà utilisée", request)
		);
	}

	@ExceptionHandler(InvalidQueryException.class)
	ResponseEntity<ProblemDetail> invalidQuery(HttpServletRequest request) {
		return body(
				HttpStatus.BAD_REQUEST,
				problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "La requête est invalide", request)
		);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<ProblemDetail> invalidCredentials(HttpServletRequest request) {
		return body(
				HttpStatus.UNAUTHORIZED,
				problem(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Identifiants invalides", request)
		);
	}

	@ExceptionHandler(DuplicateUsernameException.class)
	ResponseEntity<ProblemDetail> duplicateUsername(HttpServletRequest request) {
		return body(
				HttpStatus.CONFLICT,
				problem(HttpStatus.CONFLICT, "DUPLICATE_USERNAME", "Ce nom d'utilisateur est déjà utilisé", request)
		);
	}

	@ExceptionHandler(InsufficientAuthenticationException.class)
	ResponseEntity<ProblemDetail> unauthenticated(HttpServletRequest request) {
		return body(
				HttpStatus.UNAUTHORIZED,
				problem(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Authentification requise", request)
		);
	}

	@ExceptionHandler(AccountNotEmptyException.class)
	ResponseEntity<ProblemDetail> accountNotEmpty(HttpServletRequest request) {
		return body(
				HttpStatus.CONFLICT,
				problem(HttpStatus.CONFLICT, "ACCOUNT_NOT_EMPTY", "Le solde doit être nul pour clôturer le compte", request)
		);
	}

	@ExceptionHandler(InvalidStateTransitionException.class)
	ResponseEntity<ProblemDetail> invalidTransition(HttpServletRequest request) {
		return body(
				HttpStatus.CONFLICT,
				problem(HttpStatus.CONFLICT, "INVALID_STATE_TRANSITION", "Transition de statut interdite", request)
		);
	}

	@ExceptionHandler(CustomerSuspendedException.class)
	ResponseEntity<ProblemDetail> customerSuspended(HttpServletRequest request) {
		return body(
				HttpStatus.CONFLICT,
				problem(HttpStatus.CONFLICT, "CUSTOMER_SUSPENDED", "Le client est suspendu", request)
		);
	}

	@ExceptionHandler(DuplicatePhoneException.class)
	ResponseEntity<ProblemDetail> duplicatePhone(HttpServletRequest request) {
		return body(
				HttpStatus.CONFLICT,
				problem(HttpStatus.CONFLICT, "DUPLICATE_PHONE", "Ce téléphone est déjà utilisé", request)
		);
	}

	@ExceptionHandler(ConcurrentUpdateException.class)
	ResponseEntity<ProblemDetail> concurrentUpdate(HttpServletRequest request) {
		ProblemDetail problem = problem(
				HttpStatus.SERVICE_UNAVAILABLE,
				"CONCURRENT_UPDATE",
				"Conflit de concurrence, réessayez",
				request
		);
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.header(HttpHeaders.RETRY_AFTER, "1")
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problem);
	}

	@ExceptionHandler({
			NoResourceFoundException.class,
			HttpRequestMethodNotSupportedException.class,
			HttpMediaTypeNotSupportedException.class
	})
	ResponseEntity<ProblemDetail> framework(Exception exception, HttpServletRequest request) {
		HttpStatus status = switch (exception) {
			case NoResourceFoundException ignored -> HttpStatus.NOT_FOUND;
			case HttpRequestMethodNotSupportedException ignored -> HttpStatus.METHOD_NOT_ALLOWED;
			case HttpMediaTypeNotSupportedException ignored -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
			default -> HttpStatus.INTERNAL_SERVER_ERROR;
		};
		return body(status, problem(status, null, status.getReasonPhrase(), request));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> unexpected(Exception exception, HttpServletRequest request) {
		logUnexpected(exception);
		return body(
				HttpStatus.INTERNAL_SERVER_ERROR,
				problem(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"INTERNAL_ERROR",
						"Une erreur interne est survenue",
						request
				)
		);
	}

	private static ResponseEntity<ProblemDetail> body(HttpStatus status, ProblemDetail problem) {
		return ResponseEntity.status(status)
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problem);
	}

	private static ProblemDetail problem(HttpStatus status, String code, String detail, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(status.getReasonPhrase());
		problem.setInstance(URI.create(request.getRequestURI()));
		if (code != null) {
			problem.setProperty("code", code);
		}
		return problem;
	}

	private static String safeMessage(String message, Object rejected) {
		if (message == null || message.isBlank()) {
			return "invalide";
		}
		if (rejected != null && message.contains(String.valueOf(rejected))) {
			return "invalide";
		}
		return message;
	}

	private static void logUnexpected(Exception exception) {
		StringBuilder trace = new StringBuilder();
		Throwable current = exception;
		while (current != null) {
			trace.append(current.getClass().getName()).append(System.lineSeparator());
			for (StackTraceElement frame : current.getStackTrace()) {
				trace.append("\tat ").append(frame).append(System.lineSeparator());
			}
			current = current.getCause();
			if (current != null) {
				trace.append("Caused by: ");
			}
		}
		log.error("Unhandled API failure{}{}", System.lineSeparator(), trace);
	}

}
