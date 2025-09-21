package com.collabera.libraryapi.domain.repository;

import com.collabera.libraryapi.domain.entity.Book;
import com.collabera.libraryapi.domain.spec.Specs;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD;

public interface BookRepository extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {

    @Override
    @EntityGraph(value = "Book.catalog", type = LOAD)
    Optional<Book> findById(UUID id);

    @Override
    @EntityGraph(value = "Book.catalog", type = LOAD)
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    default Book getRequired(UUID id) {
        return findById(id).orElseThrow(() -> new RuntimeException("Book not found: " + id));
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
