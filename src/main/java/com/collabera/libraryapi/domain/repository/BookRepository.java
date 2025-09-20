package com.collabera.libraryapi.domain.repository;

import com.collabera.libraryapi.domain.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID>, JpaSpecificationExecutor<Book> {

    @EntityGraph(attributePaths = { "catalog" }, type = EntityGraphType.LOAD)
    Optional<Book> findById(UUID id);
}
