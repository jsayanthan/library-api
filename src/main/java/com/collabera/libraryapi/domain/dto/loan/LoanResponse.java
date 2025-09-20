package com.collabera.libraryapi.domain.dto.loan;

import com.collabera.libraryapi.domain.dto.book.BookResponse;
import com.collabera.libraryapi.domain.dto.borrower.BorrowerResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Loan view")
public record LoanResponse(
        @Schema(example = "a7b13a9a-1234-4c7e-9b2a-7b14a6fa9f12")
        UUID id,
        BookResponse book,
        BorrowerResponse borrower,
        @Schema(example = "2025-09-20T10:15:30Z")
        Instant borrowedAt,
        @Schema(example = "null")
        Instant returnedAt
) {}
