package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.CreateCustomerCommand;
import com.mywallet.bank.application.command.AdminAuditCommand;
import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.AuditTargetType;
import com.mywallet.bank.domain.model.Customer;
import com.mywallet.bank.domain.model.CustomerStatus;
import com.mywallet.bank.domain.port.AuditLogRepository;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class CreateCustomerExecutor {

	private final CustomerRepository customers;
	private final AuditLogRepository auditLog;
	private final Clock clock;

	public CreateCustomerExecutor(CustomerRepository customers, AuditLogRepository auditLog, Clock clock) {
		this.customers = customers;
		this.auditLog = auditLog;
		this.clock = clock;
	}

	@Transactional
	public CustomerView create(CreateCustomerCommand command) {
		Customer customer = new Customer(
				UUID.randomUUID(),
				command.firstName(),
				command.lastName(),
				command.phone(),
				blankToNull(command.email()),
				CustomerStatus.ACTIVE,
				Timestamps.now(clock)
		);
		Customer saved = customers.save(customer);
		Audits.append(auditLog, new AdminAuditCommand(
				command.actor(),
				AuditAction.CUSTOMER_CREATED,
				AuditTargetType.CUSTOMER,
				saved.getId().toString(),
				null
		), clock);
		return CustomerViews.from(saved);
	}

	private static String blankToNull(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		return email;
	}

}
