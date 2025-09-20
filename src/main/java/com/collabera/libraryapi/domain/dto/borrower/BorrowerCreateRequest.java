package com.collabera.libraryapi.domain.dto.borrower;

import com.collabera.libraryapi.validation.UniqueEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to register a borrower")
public record BorrowerCreateRequest(
        @Schema(example = "Alice Johnson")
        @NotBlank(message = "Name cannot be blank")
        String name,

        @Schema(example = "alice@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        @UniqueEmail
        String email
) {
}
