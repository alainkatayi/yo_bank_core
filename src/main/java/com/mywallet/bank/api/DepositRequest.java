package com.mywallet.bank.api;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.annotation.JsonDeserialize;

public record DepositRequest(
		@NotBlank @JsonDeserialize(using = StrictJsonStringDeserializer.class) String amount
) {
}
