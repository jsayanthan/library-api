package com.collabera.libraryapi.web.exception;
public class PageSizeLimitExceededException extends RuntimeException {
    public PageSizeLimitExceededException(String message) { super(message); }
}
