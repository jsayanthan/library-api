package com.collabera.libraryapi.service;

import com.collabera.libraryapi.domain.entity.Book;
import com.collabera.libraryapi.domain.entity.BookCatalog;
import com.collabera.libraryapi.domain.repository.BookCatalogRepository;
import com.collabera.libraryapi.domain.repository.BookRepository;
import com.collabera.libraryapi.domain.dto.book.BookCreateRequest;
import com.collabera.libraryapi.domain.dto.book.BookResponse;
import com.collabera.libraryapi.mapper.BookMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository books;
    private final BookCatalogRepository catalogs;
    private final BookMapper mapper;

    @Transactional
    public BookResponse create(BookCreateRequest request) {
        BookCatalog catalog = catalogs.findByIsbn(request.isbn())
                .orElseGet(() -> catalogs.save(mapper.toCatalog(request)));
        Book book = Book.builder().catalog(catalog).borrowed(false).build();
        return mapper.toResponse(books.save(book));
    }

    public Page<BookResponse> list(String search, Pageable pageable) {
        return books.findAll(BookRepository.bySearch(search), pageable).map(mapper::toResponse);
    }
}
