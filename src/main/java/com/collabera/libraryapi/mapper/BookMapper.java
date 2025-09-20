package com.collabera.libraryapi.mapper;

import com.collabera.libraryapi.domain.dto.book.BookCreateRequest;
import com.collabera.libraryapi.domain.entity.Book;
import com.collabera.libraryapi.domain.dto.book.BookResponse;
import com.collabera.libraryapi.domain.entity.BookCatalog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookCatalog toCatalog(BookCreateRequest request);

    @Mapping(target = "isbn", source = "catalog.isbn")
    @Mapping(target = "title", source = "catalog.title")
    @Mapping(target = "author", source = "catalog.author")
    BookResponse toResponse(Book book);
}
