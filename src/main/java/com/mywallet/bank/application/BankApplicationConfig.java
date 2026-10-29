package com.mywallet.bank.application;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(RetryProperties.class)
public class BankApplicationConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}
