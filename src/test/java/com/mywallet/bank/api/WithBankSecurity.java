package com.mywallet.bank.api;

import com.mywallet.bank.infrastructure.security.BankAccessDeniedHandler;
import com.mywallet.bank.infrastructure.security.BankAuthenticationEntryPoint;
import com.mywallet.bank.infrastructure.security.BankJwtAuthenticationConverter;
import com.mywallet.bank.infrastructure.security.BankSecurityConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({
		BankSecurityConfiguration.class,
		BankJwtAuthenticationConverter.class,
		BankAuthenticationEntryPoint.class,
		BankAccessDeniedHandler.class
})
public @interface WithBankSecurity {
}
