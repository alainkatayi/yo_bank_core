package com.mywallet.bank.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyLinkRequest(
		@NotBlank @Pattern(regexp = "\\d{12}") String accountNumber,
		@NotBlank @Size(max = 20) String phone
) {
}
