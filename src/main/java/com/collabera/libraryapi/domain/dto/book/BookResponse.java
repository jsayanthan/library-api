package com.collabera.libraryapi.domain.dto.book;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Book copy view")
public record BookResponse(
        @Schema(example = "f8f6a9f2-24de-4f61-9c76-f0f74b07a99b")
        UUID id,
        @Schema(example = "9780134685991")
        String isbn,
        @Schema(example = "Effective Java")
        String title,
        @Schema(example = "Joshua Bloch")
        String author,
        @Schema(example = "false")
        boolean borrowed
) {
}
