package com.mywallet.bank.application.service;

import com.mywallet.bank.application.port.in.ListAgentsUseCase;
import com.mywallet.bank.application.result.AgentView;
import com.mywallet.bank.domain.port.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAgentsService implements ListAgentsUseCase {

	private final AgentRepository agents;

	public ListAgentsService(AgentRepository agents) {
		this.agents = agents;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AgentView> execute() {
		return agents.findAll().stream().map(CreateAgentService::toView).toList();
	}

}
