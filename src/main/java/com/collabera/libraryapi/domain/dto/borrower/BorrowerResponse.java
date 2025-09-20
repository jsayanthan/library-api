package com.collabera.libraryapi.domain.dto.borrower;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Borrower view")
public record BorrowerResponse(
        @Schema(example = "b3b83a9a-94a3-4d7e-9b2a-7b14a6fa9f12")
        UUID id,
        @Schema(example = "Alice Johnson")
        String name,
        @Schema(example = "alice@example.com")
        String email
) {
}
