package com.collabera.libraryapi.domain.repository;

import com.collabera.libraryapi.domain.entity.BookCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCatalogRepository extends JpaRepository<BookCatalog, String> {
    boolean existsByIsbn(String isbn);
}
