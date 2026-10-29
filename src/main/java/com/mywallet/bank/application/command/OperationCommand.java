package com.mywallet.bank.application.command;

public record OperationCommand(String reference, String accountNumber, String amount) {
}
