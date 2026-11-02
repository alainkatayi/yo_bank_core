package com.mywallet.bank.application.service;

import java.util.UUID;

record PreparedLink(UUID linkId, int expiresIn, String email, String code) {
}
