package com.mywallet.bank.application.port.out;

import java.util.UUID;

public interface OtpSender {

	void send(UUID linkId, String recipient, String code);

}
