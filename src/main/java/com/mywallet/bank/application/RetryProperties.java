package com.mywallet.bank.application;

import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bank.retry")
public record RetryProperties(@DefaultValue("5") int maxAttempts) {

	public RetryProperties {
		if (maxAttempts < 1) {
			throw new IllegalArgumentException("bank.retry.max-attempts doit être supérieur ou égal à 1");
		}
	}

}
