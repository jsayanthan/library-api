package com.collabera.libraryapi.domain.repository;

import com.collabera.libraryapi.domain.entity.BookCatalog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;
import java.util.UUID;

public interface BookCatalogRepository extends JpaRepository<BookCatalog, UUID>, JpaSpecificationExecutor<BookCatalog> {
    Optional<BookCatalog> findByIsbn(String isbn);
    boolean existsByIsbnIgnoreCase(String isbn);

    default boolean isIsbnUnique(UUID excludeId, String isbn) {
        Specification<BookCatalog> spec = (root, q, cb) -> cb.equal(cb.lower(root.get("isbn")), isbn.toLowerCase());
        if (excludeId != null) spec = spec.and((root, q, cb) -> cb.notEqual(root.get("id"), excludeId));
        return findAll(spec).isEmpty();
    }
}

