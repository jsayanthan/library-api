package com.collabera.libraryapi.web.exception;

public class IsbnMetadataMismatchException extends RuntimeException {
    public IsbnMetadataMismatchException(String isbn) {
        super("Title/author do not match existing catalog entry for ISBN " + isbn);
    }
}
