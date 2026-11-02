package com.mywallet.bank.api;

import java.util.UUID;

public record InitiateLinkResponse(UUID linkId, int expiresIn) {
}
