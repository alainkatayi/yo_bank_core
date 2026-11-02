package com.mywallet.bank.infrastructure.mail;

import com.mywallet.bank.application.port.out.OtpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EmailOtpSender implements OtpSender {

	private static final Logger log = LoggerFactory.getLogger(EmailOtpSender.class);

	private final JavaMailSender mail;
	private final String from;
	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	public EmailOtpSender(JavaMailSender mail, @Value("${app.mail.from}") String from) {
		this.mail = mail;
		this.from = from;
	}

	@Override
	public void send(UUID linkId, String recipient, String code) {
		executor.execute(() -> deliver(linkId, recipient, code));
	}

	private void deliver(UUID linkId, String recipient, String code) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(recipient);
			message.setSubject("Votre code de vérification YO! Bank");
			message.setText("""
					Bonjour,

					Votre code de vérification est : %s
					Il est valable 5 minutes.
					Ne le communiquez jamais à qui que ce soit.

					Yo! Bank is my bank
					""".formatted(code));
			mail.send(message);
		} catch (RuntimeException exception) {
			log.error("Echec de l'envoi du code de liaison linkId={} error={}", linkId, exception.getClass().getName());
		}
	}

}
