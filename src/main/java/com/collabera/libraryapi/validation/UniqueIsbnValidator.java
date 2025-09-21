package com.collabera.libraryapi.validation;

import com.collabera.libraryapi.domain.repository.BookCatalogRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueIsbnValidator implements ConstraintValidator<UniqueIsbn, String> {

    private final BookCatalogRepository bookCatalogRepository;

    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {
        if (isbn == null || isbn.isBlank()) return true;
        String normalized = isbn.replaceAll("[\\s-]", "").toUpperCase();
        return !bookCatalogRepository.existsByIsbnIgnoreCase(normalized);
    }
}
