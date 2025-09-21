package com.collabera.libraryapi.web.exception;
import java.util.UUID;

public class BorrowerNotFoundException extends RuntimeException {
    public BorrowerNotFoundException(UUID id) { super("Borrower not found: " + id); }
}
