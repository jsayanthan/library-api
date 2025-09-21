package com.collabera.libraryapi.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BorrowerNotFoundException.class)
    public ResponseEntity<ApiError> borrowerNotFound(BorrowerNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("BORROWER_NOT_FOUND", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ApiError> bookNotFound(BookNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("BOOK_NOT_FOUND", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(BookAlreadyBorrowedException.class)
    public ResponseEntity<ApiError> borrowed(BookAlreadyBorrowedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("BOOK_ALREADY_BORROWED", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(PageSizeLimitExceededException.class)
    public ResponseEntity<ApiError> pageLimit(PageSizeLimitExceededException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("PAGE_SIZE_LIMIT", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiError.of("VALIDATION_ERROR", msg, req.getRequestURI()));
    }

    /**
     * Map DB unique/foreign key errors by named constraints for clean messages.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dbConstraint(DataIntegrityViolationException ex, HttpServletRequest req) {
        String cause = (ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "").toLowerCase(Locale.ROOT);
        String code = "CONSTRAINT_VIOLATION";
        String message = "Constraint violation";

        if (cause.contains("uk_borrowers_email")) {
            code = "EMAIL_ALREADY_EXISTS";
            message = "Email already exists";
        } else if (cause.contains("uk_book_catalog_isbn")) {
            code = "ISBN_ALREADY_EXISTS";
            message = "ISBN already exists";
        } else if (cause.contains("uq_loans_book_active")) {
            code = "BOOK_ALREADY_BORROWED";
            message = "Book already has an active loan";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(code, message, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "Unexpected error", req.getRequestURI()));
    }
}
