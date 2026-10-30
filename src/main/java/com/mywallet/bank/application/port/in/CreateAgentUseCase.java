package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.command.CreateAgentCommand;
import com.mywallet.bank.application.result.AgentView;

public interface CreateAgentUseCase {

	AgentView execute(CreateAgentCommand command);

}
