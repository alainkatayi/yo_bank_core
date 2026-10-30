package com.mywallet.bank.domain.port;

import com.mywallet.bank.domain.model.BankAgent;

import java.util.List;
import java.util.Optional;

public interface AgentRepository {

	Optional<BankAgent> findByUsername(String username);

	boolean existsByUsername(String username);

	List<BankAgent> findAll();

	BankAgent save(BankAgent agent);

}
