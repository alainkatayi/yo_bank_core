package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.result.AgentView;

import java.util.List;

public interface ListAgentsUseCase {

	List<AgentView> execute();

}
