package com.mywallet.bank.application.service;

import com.mywallet.bank.application.command.LoginCommand;
import com.mywallet.bank.application.exception.InvalidCredentialsException;
import com.mywallet.bank.application.port.in.LoginAgentUseCase;
import com.mywallet.bank.application.port.out.PasswordChecker;
import com.mywallet.bank.application.port.out.AccessTokenIssuer;
import com.mywallet.bank.application.result.LoginResult;
import com.mywallet.bank.domain.model.BankAgent;
import com.mywallet.bank.domain.port.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAgentService implements LoginAgentUseCase {

	private static final String DUMMY_HASH = "$2a$10$tErDg8BNN4FRL8UJdhttU.1P32T2ueQ0.a8JngZvFe4arOcKewljW";

	private final AgentRepository agents;
	private final PasswordChecker passwords;
	private final AccessTokenIssuer tokens;

	public LoginAgentService(AgentRepository agents, PasswordChecker passwords, AccessTokenIssuer tokens) {
		this.agents = agents;
		this.passwords = passwords;
		this.tokens = tokens;
	}

	@Override
	@Transactional(readOnly = true)
	public LoginResult execute(LoginCommand command) {
		if (command == null) {
			throw new IllegalArgumentException("La commande est obligatoire");
		}
		BankAgent agent = agents.findByUsername(command.username().trim()).orElse(null);
		String hash = agent == null ? DUMMY_HASH : agent.getPasswordHash();
		boolean matches = passwords.matches(command.password(), hash);
		if (agent == null || !agent.isEnabled() || !matches) {
			throw new InvalidCredentialsException();
		}
		return new LoginResult(tokens.issue(agent), CreateAgentService.toView(agent));
	}

}
