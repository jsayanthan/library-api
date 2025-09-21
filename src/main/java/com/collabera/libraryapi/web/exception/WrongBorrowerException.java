package com.collabera.libraryapi.web.exception;

import java.util.UUID;

public class WrongBorrowerException extends RuntimeException {
    public WrongBorrowerException(UUID bookId) {
        super("This book is currently borrowed by a different borrower: " + bookId);
    }
}
