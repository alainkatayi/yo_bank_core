package com.mywallet.bank.application.result;

import java.util.UUID;

public record InitiateLinkResult(UUID linkId, int expiresIn) {
}
