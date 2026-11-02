package com.mywallet.bank.api;

public record ConfirmLinkResponse(boolean verified, String accountNumber) {
}
