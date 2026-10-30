package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.AdminAuditCommand;
import com.mywallet.bank.application.command.CreateAgentCommand;
import com.mywallet.bank.application.exception.DuplicateUsernameException;
import com.mywallet.bank.application.port.in.CreateAgentUseCase;
import com.mywallet.bank.application.port.out.PasswordChecker;
import com.mywallet.bank.application.result.AgentView;
import com.mywallet.bank.domain.model.AuditAction;
import com.mywallet.bank.domain.model.AuditTargetType;
import com.mywallet.bank.domain.model.BankAgent;
import com.mywallet.bank.domain.port.AgentRepository;
import com.mywallet.bank.domain.port.AuditLogRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class CreateAgentService implements CreateAgentUseCase {

	private final AgentRepository agents;
	private final AuditLogRepository auditLog;
	private final PasswordChecker passwords;
	private final Clock clock;

	public CreateAgentService(
			AgentRepository agents,
			AuditLogRepository auditLog,
			PasswordChecker passwords,
			Clock clock
	) {
		this.agents = agents;
		this.auditLog = auditLog;
		this.passwords = passwords;
		this.clock = clock;
	}

	@Override
	@Transactional
	public AgentView execute(CreateAgentCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("La commande est obligatoire");
		}
		String username = command.username().trim();
		if (agents.existsByUsername(username)) {
			throw new DuplicateUsernameException();
		}
		String email = command.email() == null || command.email().isBlank() ? null : command.email().trim();
		BankAgent agent = new BankAgent(
				UUID.randomUUID(),
				username,
				passwords.hash(command.password()),
				command.firstName().trim(),
				command.lastName().trim(),
				email,
				command.role(),
				true,
				Timestamps.now(clock)
		);
		BankAgent saved;
		try {
			saved = agents.save(agent);
		} catch (DataIntegrityViolationException exception) {
			if (UniqueConstraints.matches(exception, "username")) {
				throw new DuplicateUsernameException();
			}
			throw exception;
		}
		Audits.append(auditLog, new AdminAuditCommand(
				command.actor(),
				AuditAction.AGENT_CREATED,
				AuditTargetType.AGENT,
				saved.getId().toString(),
				"username=" + saved.getUsername()
		), clock);
		return toView(saved);
	}

	static AgentView toView(BankAgent agent) {
		return new AgentView(
				agent.getId(),
				agent.getUsername(),
				agent.getFirstName(),
				agent.getLastName(),
				agent.getEmail(),
				agent.getRole(),
				agent.isEnabled(),
				agent.getCreatedAt()
		);
	}

}
