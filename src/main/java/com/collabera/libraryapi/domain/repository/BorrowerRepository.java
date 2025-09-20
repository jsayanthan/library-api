package com.collabera.libraryapi.domain.repository;

import com.collabera.libraryapi.domain.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BorrowerRepository extends JpaRepository<Borrower, UUID> {
    boolean existsByEmailIgnoreCase(String email);
}
