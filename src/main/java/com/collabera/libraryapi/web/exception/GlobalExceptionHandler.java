package com.collabera.libraryapi.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Locale;
import java.util.stream.Collectors;
import static com.collabera.libraryapi.core.constants.ErrorCodes.*;
import static com.collabera.libraryapi.core.constants.DbConstraints.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BorrowerNotFoundException.class)
    public ResponseEntity<ApiError> borrowerNotFound(BorrowerNotFoundException ex, HttpServletRequest req) {
        return notFound(BORROWER_NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ApiError> bookNotFound(BookNotFoundException ex, HttpServletRequest req) {
        return notFound(BOOK_NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(BookAlreadyBorrowedException.class)
    public ResponseEntity<ApiError> borrowed(BookAlreadyBorrowedException ex, HttpServletRequest req) {
        return conflict(BOOK_ALREADY_BORROWED, ex.getMessage(), req);
    }

    @ExceptionHandler(ActiveLoanNotFoundException.class)
    public ResponseEntity<ApiError> noActiveLoan(ActiveLoanNotFoundException ex, HttpServletRequest req) {
        return notFound(ACTIVE_LOAN_NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> optimistic(OptimisticLockingFailureException ex, HttpServletRequest req) {
        return conflict(CONCURRENT_MODIFICATION, "Concurrent update detected. Retry the operation.", req);
    }

    @ExceptionHandler(IsbnMetadataMismatchException.class)
    public ResponseEntity<ApiError> isbnMismatch(IsbnMetadataMismatchException ex, HttpServletRequest req) {
        return conflict(ISBN_METADATA_MISMATCH, ex.getMessage(), req);
    }

    @ExceptionHandler(WrongBorrowerException.class)
    public ResponseEntity<ApiError> wrongBorrower(WrongBorrowerException ex, HttpServletRequest req) {
        return conflict(WRONG_BORROWER, ex.getMessage(), req);
    }

    @ExceptionHandler(PageSizeLimitExceededException.class)
    public ResponseEntity<ApiError> pageLimit(PageSizeLimitExceededException ex, HttpServletRequest req) {
        return badRequest(PAGE_SIZE_LIMIT, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
    return badRequest(VALIDATION_ERROR, msg, req);
    }

    /**
     * Map DB unique/foreign key errors by named constraints for clean messages.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dbConstraint(DataIntegrityViolationException ex, HttpServletRequest req) {
        String cause = (ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "").toLowerCase(Locale.ROOT);
        String code = CONSTRAINT_VIOLATION;
        String message = "Constraint violation";

        if (cause.contains(UK_BORROWERS_EMAIL)) {
            code = EMAIL_ALREADY_EXISTS;
            message = "Email already exists";
        } else if (cause.contains(UK_BOOK_CATALOG_ISBN)) {
            code = ISBN_ALREADY_EXISTS;
            message = "ISBN already exists";
        } else if (cause.contains(UQ_LOANS_BOOK_ACTIVE)) {
            code = BOOK_ALREADY_BORROWED;
            message = "Book already has an active loan";
        }

        return conflict(code, message, req);
    }

    /**
     * Suppress noisy 404 logs for static resource probes (e.g. Chrome DevTools /.well-known paths).
     * We log at debug rather than error so genuine problems are more visible.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> staticResourceNotFound(NoResourceFoundException ex, HttpServletRequest req) {
        if (log.isDebugEnabled()) {
            log.debug("Static resource not found: {}", req.getRequestURI());
        }
        return notFound(NOT_FOUND, "Resource not found", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(INTERNAL_ERROR, "Unexpected error", req.getRequestURI()));
    }

    // --- Helper builders --------------------------------------------------
    private ResponseEntity<ApiError> notFound(String code, String msg, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(code, msg, req.getRequestURI()));
    }
    private ResponseEntity<ApiError> conflict(String code, String msg, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(code, msg, req.getRequestURI()));
    }
    private ResponseEntity<ApiError> badRequest(String code, String msg, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(ApiError.of(code, msg, req.getRequestURI()));
    }
}
