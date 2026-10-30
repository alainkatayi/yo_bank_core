package com.mywallet.bank.application.service;

import com.mywallet.bank.application.exception.CustomerNotFoundException;
import com.mywallet.bank.application.port.in.OpenAccountUseCase;
import com.mywallet.bank.application.result.AccountView;
import com.mywallet.bank.domain.model.AccountNumber;
import com.mywallet.bank.domain.model.Customer;
import com.mywallet.bank.domain.port.CustomerRepository;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Ne jamais appeler ce bean depuis une transaction déjà ouverte.
 * Chaque tentative d'insertion passe par {@link AccountOpeningExecutor},
 * qui ouvre puis ferme sa propre transaction. Une collision de numéro
 * condamne cette transaction : la suivante doit en ouvrir une nouvelle.
 */
@Service
public class OpenAccountService implements OpenAccountUseCase {

	static final int MAX_ATTEMPTS = 5;

	private final CustomerRepository customers;
	private final AccountOpeningExecutor opener;
	private final SecureRandom random = new SecureRandom();

	public OpenAccountService(CustomerRepository customers, AccountOpeningExecutor opener) {
		this.customers = customers;
		this.opener = opener;
	}

	@Override
	public AccountView execute(UUID customerId) {
		if (customerId == null) {
			throw new IllegalArgumentException("L'identifiant client est obligatoire");
		}
		Customer customer = customers.findById(customerId)
				.orElseThrow(CustomerNotFoundException::new);
		for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
			try {
				return AccountViews.from(opener.open(customer.getId(), nextAccountNumber()), customer);
			} catch (AccountNumberTakenException ignored) {
				// Une autre création a pris ce numéro : on en tire un autre.
			}
		}
		throw new IllegalStateException("Impossible d'attribuer un numéro de compte");
	}

	private AccountNumber nextAccountNumber() {
		StringBuilder digits = new StringBuilder(12);
		for (int index = 0; index < 12; index++) {
			digits.append(random.nextInt(10));
		}
		return new AccountNumber(digits.toString());
	}

}
