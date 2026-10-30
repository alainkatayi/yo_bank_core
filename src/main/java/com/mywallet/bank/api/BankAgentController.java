package com.mywallet.bank.api;

import com.mywallet.bank.application.command.CreateAgentCommand;
import com.mywallet.bank.application.command.LoginCommand;
import com.mywallet.bank.application.port.in.CreateAgentUseCase;
import com.mywallet.bank.application.port.in.ListAgentsUseCase;
import com.mywallet.bank.application.port.in.LoginAgentUseCase;
import com.mywallet.bank.application.result.AgentView;
import com.mywallet.bank.application.result.LoginResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class BankAgentController {

	private final LoginAgentUseCase login;
	private final CreateAgentUseCase creation;
	private final ListAgentsUseCase agents;

	public BankAgentController(
			LoginAgentUseCase login,
			CreateAgentUseCase creation,
			ListAgentsUseCase agents
	) {
		this.login = login;
		this.creation = creation;
		this.agents = agents;
	}

	@PostMapping("/bank/auth/login")
	public TokenResponse login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = login.execute(new LoginCommand(request.username(), request.password()));
		return new TokenResponse(
				result.token().value(),
				"Bearer",
				result.token().expiresInSeconds(),
				toResponse(result.agent())
		);
	}

	@PostMapping("/bank/admin/agents")
	public ResponseEntity<AgentResponse> create(@Valid @RequestBody CreateAgentRequest request) {
		AgentView agent = creation.execute(new CreateAgentCommand(
				CurrentAdmin.get(),
				request.username(),
				request.password(),
				request.firstName(),
				request.lastName(),
				request.email(),
				request.role()
		));
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(agent.id())
				.toUri();
		return ResponseEntity.created(location).body(toResponse(agent));
	}

	@GetMapping("/bank/admin/agents")
	public List<AgentResponse> list() {
		return agents.execute().stream().map(BankAgentController::toResponse).toList();
	}

	private static AgentResponse toResponse(AgentView agent) {
		return new AgentResponse(
				agent.id().toString(),
				agent.username(),
				agent.firstName(),
				agent.lastName(),
				agent.email(),
				agent.role().name(),
				agent.enabled(),
				agent.createdAt()
		);
	}

}
