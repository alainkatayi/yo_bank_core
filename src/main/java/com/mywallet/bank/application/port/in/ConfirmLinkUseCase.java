package com.mywallet.bank.application.port.in;

import com.mywallet.bank.application.result.ConfirmLinkResult;

import java.util.UUID;

public interface ConfirmLinkUseCase {

	ConfirmLinkResult execute(UUID linkId, String code);

}
