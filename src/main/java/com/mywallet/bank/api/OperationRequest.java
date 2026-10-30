package com.mywallet.bank.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

public record OperationRequest(
		@NotBlank @Size(max = 64) String reference,
		@NotBlank @Pattern(regexp = "\\d{12}") String accountNumber,
		@NotBlank @JsonDeserialize(using = StrictJsonStringDeserializer.class) String amount
) {
}
