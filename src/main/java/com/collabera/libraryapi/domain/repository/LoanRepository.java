package com.collabera.libraryapi.domain.repository;

import com.collabera.libraryapi.domain.entity.Loan;
import org.springframework.data.jpa.repository.*;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    @EntityGraph(value = "Loan.full", type = LOAD)
    Optional<Loan> findByBookIdAndReturnedAtIsNull(UUID bookId);
}
