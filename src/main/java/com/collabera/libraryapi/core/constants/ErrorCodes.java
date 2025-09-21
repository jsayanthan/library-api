package com.collabera.libraryapi.core.constants;

public final class ErrorCodes {
    private ErrorCodes() {}

    // Generic
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String CONSTRAINT_VIOLATION = "CONSTRAINT_VIOLATION";

    // Domain specific
    public static final String BORROWER_NOT_FOUND = "BORROWER_NOT_FOUND";
    public static final String BOOK_NOT_FOUND = "BOOK_NOT_FOUND";
    public static final String ACTIVE_LOAN_NOT_FOUND = "ACTIVE_LOAN_NOT_FOUND";
    public static final String BOOK_ALREADY_BORROWED = "BOOK_ALREADY_BORROWED";
    public static final String CONCURRENT_MODIFICATION = "CONCURRENT_MODIFICATION";
    public static final String PAGE_SIZE_LIMIT = "PAGE_SIZE_LIMIT";
    public static final String ISBN_METADATA_MISMATCH = "ISBN_METADATA_MISMATCH";
    public static final String WRONG_BORROWER = "WRONG_BORROWER";

    // Data constraints
    public static final String EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    public static final String ISBN_ALREADY_EXISTS = "ISBN_ALREADY_EXISTS";
}
