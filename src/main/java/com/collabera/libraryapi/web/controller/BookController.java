package com.collabera.libraryapi.web.controller;

import com.collabera.libraryapi.aop.annotations.PageSizeLimit;
import com.collabera.libraryapi.core.util.PageResponse;
import com.collabera.libraryapi.core.util.Paging;
import com.collabera.libraryapi.domain.dto.book.BookCreateRequest;
import com.collabera.libraryapi.domain.dto.book.BookResponse;
import com.collabera.libraryapi.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Books")
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService service;

    @Operation(summary = "Register a new book copy (catalog deduplicated by ISBN)")
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody BookCreateRequest request) {
        return service.create(request);
    }

    @Operation(summary = "List books (search by title/author/isbn)")
    @PageSizeLimit(max = 50)
    @GetMapping
    public PageResponse<BookResponse> list(
            @RequestParam(required = false, name = "search") String search,
            @ParameterObject
            @PageableDefault(size = 20)
            @SortDefault(sort = "catalog.title")
            Pageable pageable
    ) {
        Page<BookResponse> page = service.list(search, pageable);
        return Paging.toResponse(page, pageable);
    }
}
