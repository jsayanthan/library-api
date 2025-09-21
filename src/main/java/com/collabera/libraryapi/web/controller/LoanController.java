package com.collabera.libraryapi.web.controller;

import com.collabera.libraryapi.domain.dto.loan.LoanCreateRequest;
import com.collabera.libraryapi.domain.dto.loan.LoanResponse;
import com.collabera.libraryapi.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Loans")
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService service;

    @Operation(summary = "Borrow a book")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse borrow(@Valid @RequestBody LoanCreateRequest request) {
        return service.borrow(request);
    }

    @Operation(summary = "Return a borrowed book")
    @PostMapping("/return")
    @ResponseStatus(HttpStatus.OK)
    public LoanResponse returnBook(@Valid @RequestBody LoanCreateRequest request) {
        return service.returnBook(request);
    }
}
