package com.collabera.libraryapi.domain.dto.book;

import com.collabera.libraryapi.validation.UniqueIsbn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to create a new book catalog entry (and allow copies)")
public record BookCreateRequest(

        @Schema(description = "ISBN-10 or ISBN-13", example = "9780134685991")
        @NotBlank(message = "ISBN cannot be blank")
        @Pattern(
                regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[-0-9Xx ]{10,17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9Xx]$",
                message = "Invalid ISBN-10/13 format"
        )
        @UniqueIsbn
        String isbn,

        @Schema(description = "Title", example = "Effective Java")
        @NotBlank(message = "Title cannot be blank")
        String title,

        @Schema(description = "Author", example = "Joshua Bloch")
        @NotBlank(message = "Author cannot be blank")
        String author
) {
}
