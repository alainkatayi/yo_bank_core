package com.mywallet.bank.api;

import com.mywallet.bank.domain.model.AgentRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAgentRequest(
		@NotBlank @Size(min = 3, max = 100) @Pattern(regexp = "[A-Za-z0-9._-]+") String username,
		@NotBlank @Size(min = 8, max = 100) String password,
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@Email @Size(max = 150) String email,
		@NotNull AgentRole role
) {
}
