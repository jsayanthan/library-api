package com.collabera.libraryapi.domain.dto.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to borrow a book")
public record LoanCreateRequest(
        @Schema(example = "f8f6a9f2-24de-4f61-9c76-f0f74b07a99b")
        @NotNull(message = "Book ID is required")
        UUID bookId,
        @Schema(example = "b3b83a9a-94a3-4d7e-9b2a-7b14a6fa9f12")
        @NotNull(message = "Borrower ID is required")
        UUID borrowerId
) {
}
