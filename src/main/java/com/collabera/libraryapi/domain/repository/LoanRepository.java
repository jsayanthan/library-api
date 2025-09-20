package com.collabera.libraryapi.domain.repository;

import com.collabera.libraryapi.domain.entity.Loan;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;

import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID>, JpaSpecificationExecutor<Loan> {

    @EntityGraph(attributePaths = { "book", "book.catalog", "borrower" }, type = EntityGraphType.LOAD)
    Optional<Loan> findById(UUID id);
}
