package com.mywallet.bank.infrastructure.persistence;

import com.mywallet.bank.domain.model.BankAgent;
import com.mywallet.bank.domain.port.AgentRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class AgentRepositoryAdapter implements AgentRepository {

	private final SpringDataAgentRepository agents;

	public AgentRepositoryAdapter(SpringDataAgentRepository agents) {
		this.agents = agents;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<BankAgent> findByUsername(String username) {
		return agents.findByUsername(username).map(AgentRepositoryAdapter::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByUsername(String username) {
		return agents.existsByUsername(username);
	}

	@Override
	@Transactional(readOnly = true)
	public List<BankAgent> findAll() {
		return agents.findAllByOrderByUsernameAsc().stream().map(AgentRepositoryAdapter::toDomain).toList();
	}

	@Override
	@Transactional
	public BankAgent save(BankAgent agent) {
		BankAgentJpaEntity entity = new BankAgentJpaEntity(
				agent.getId(),
				agent.getUsername(),
				agent.getPasswordHash(),
				agent.getFirstName(),
				agent.getLastName(),
				agent.getEmail(),
				agent.getRole(),
				agent.isEnabled(),
				agent.getCreatedAt()
		);
		return toDomain(agents.saveAndFlush(entity));
	}

	private static BankAgent toDomain(BankAgentJpaEntity entity) {
		return new BankAgent(
				entity.getId(),
				entity.getUsername(),
				entity.getPasswordHash(),
				entity.getFirstName(),
				entity.getLastName(),
				entity.getEmail(),
				entity.getRole(),
				entity.isEnabled(),
				entity.getCreatedAt()
		);
	}

}
