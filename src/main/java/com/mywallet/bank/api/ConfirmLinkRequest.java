package com.mywallet.bank.api;

public record ConfirmLinkRequest(String linkId, String code) {
}
