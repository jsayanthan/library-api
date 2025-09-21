package com.collabera.libraryapi.web.exception;
import java.util.UUID;

public class BookAlreadyBorrowedException extends RuntimeException {
    public BookAlreadyBorrowedException(UUID id) { super("Book is already borrowed: " + id); }
}
