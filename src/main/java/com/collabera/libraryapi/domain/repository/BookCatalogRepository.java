package com.collabera.libraryapi.domain.repository;

import com.collabera.libraryapi.domain.entity.BookCatalog;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;
public interface BookCatalogRepository extends JpaRepository<BookCatalog, String>, JpaSpecificationExecutor<BookCatalog> {
    Optional<BookCatalog> findByIsbnIgnoreCase(String isbn);
    boolean existsByIsbnIgnoreCase(String isbn);
}

