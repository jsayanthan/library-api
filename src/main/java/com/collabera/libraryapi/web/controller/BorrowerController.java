package com.collabera.libraryapi.web.controller;

import com.collabera.libraryapi.domain.dto.borrower.BorrowerCreateRequest;
import com.collabera.libraryapi.domain.dto.borrower.BorrowerResponse;
import com.collabera.libraryapi.service.BorrowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Borrowers")
@RestController
@RequestMapping("/api/v1/borrowers")
@RequiredArgsConstructor
public class BorrowerController {
    private final BorrowerService service;

    @Operation(summary = "Register a new borrower (email is unique)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowerResponse register(@Valid @RequestBody BorrowerCreateRequest request) {
        return service.register(request);
    }
}
