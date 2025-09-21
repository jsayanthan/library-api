package com.collabera.libraryapi.domain.repository;

import com.collabera.libraryapi.domain.entity.Book;
import com.collabera.libraryapi.domain.spec.Specs;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.Nullable;
import org.springframework.lang.NonNull;
import com.collabera.libraryapi.web.exception.BookNotFoundException;

import java.util.UUID;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {

    default Book getRequired(UUID id) {
        return findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    static Specification<Book> bySearch(String search) {
        if (search == null || search.isBlank()) return Specs.truth();
        String like = "%" + search.toLowerCase() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("catalog").get("title")),  like),
                cb.like(cb.lower(root.get("catalog").get("author")), like),
                cb.like(cb.lower(root.get("catalog").get("isbn")),   like)
        );
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Book b set b.borrowed = true where b.id = :id and b.borrowed = false")
    int markBorrowedIfAvailable(UUID id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Book b set b.borrowed = false where b.id = :id and b.borrowed = true")
    int markReturned(UUID id);

    @Override
    @EntityGraph(attributePaths = "catalog")
    @NonNull
    Page<Book> findAll(@Nullable Specification<Book> spec, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = "catalog")
    Optional<Book> findWithCatalogById(UUID id);

    @UtilityClass
    final class BookSpecs {
        public static Specification<Book> titleLike(String s) {
            if (s == null || s.isBlank()) return null;
            String like = "%" + s.toLowerCase() + "%";
            return (r,q,cb) -> cb.like(cb.lower(r.get("catalog").get("title")), like);
        }
        public static Specification<Book> authorLike(String s) {
            if (s == null || s.isBlank()) return null;
            String like = "%" + s.toLowerCase() + "%";
            return (r,q,cb) -> cb.like(cb.lower(r.get("catalog").get("author")), like);
        }
        public static Specification<Book> isbnLike(String s) {
            if (s == null || s.isBlank()) return null;
            String like = "%" + s.toLowerCase() + "%";
            return (r,q,cb) -> cb.like(cb.lower(r.get("catalog").get("isbn")), like);
        }
    }
}
