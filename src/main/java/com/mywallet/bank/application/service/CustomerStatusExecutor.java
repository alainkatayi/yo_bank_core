package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.AdminAuditCommand;
import com.mywallet.bank.application.command.CustomerStatusCommand;
import com.mywallet.bank.application.exception.CustomerNotFoundException;
import com.mywallet.bank.application.result.CustomerView;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.AuditTargetType;
import com.mywallet.bank.domain.model.Customer;
import com.mywallet.bank.domain.port.AuditLogRepository;
import com.mywallet.bank.domain.port.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class CustomerStatusExecutor {

	private final CustomerRepository customers;
	private final AuditLogRepository auditLog;
	private final Clock clock;

	public CustomerStatusExecutor(CustomerRepository customers, AuditLogRepository auditLog, Clock clock) {
		this.customers = customers;
		this.auditLog = auditLog;
		this.clock = clock;
	}

	@Transactional
	public CustomerView suspend(CustomerStatusCommand command) {
		return change(command, true);
	}

	@Transactional
	public CustomerView reactivate(CustomerStatusCommand command) {
		return change(command, false);
	}

	private CustomerView change(CustomerStatusCommand command, boolean suspend) {
		if (command == null) {
			throw new IllegalArgumentException("La commande est obligatoire");
		}
		Customer customer = customers.findById(command.customerId())
				.orElseThrow(CustomerNotFoundException::new);
		if (suspend) {
			customer.suspend();
		} else {
			customer.reactivate();
		}
		Customer saved = customers.save(customer);
		Audits.append(auditLog, new AdminAuditCommand(
				command.actor(),
				suspend ? AuditAction.CUSTOMER_SUSPENDED : AuditAction.CUSTOMER_REACTIVATED,
				AuditTargetType.CUSTOMER,
				saved.getId().toString(),
				null
		), clock);
		return CustomerViews.from(saved);
	}

}
