package com.collabera.libraryapi.web.exception;

import java.util.UUID;

public class ActiveLoanNotFoundException extends RuntimeException {
    public ActiveLoanNotFoundException(UUID bookId) {
        super("No active loan found for book: " + bookId);
    }
}
