package com.collabera.libraryapi.service;

import com.collabera.libraryapi.domain.entity.Loan;
import com.collabera.libraryapi.domain.repository.BookRepository;
import com.collabera.libraryapi.domain.repository.BorrowerRepository;
import com.collabera.libraryapi.domain.repository.LoanRepository;
import com.collabera.libraryapi.domain.dto.loan.LoanCreateRequest;
import com.collabera.libraryapi.domain.dto.loan.LoanResponse;
import com.collabera.libraryapi.mapper.LoanMapper;
import com.collabera.libraryapi.web.exception.BookAlreadyBorrowedException;
import com.collabera.libraryapi.web.exception.BookNotFoundException;
import com.collabera.libraryapi.web.exception.BorrowerNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final LoanRepository loans;
    private final BookRepository books;
    private final BorrowerRepository borrowers;
    private final LoanMapper mapper;

    @Transactional
    public LoanResponse borrow(LoanCreateRequest req) {
        var borrower = borrowers.findById(req.borrowerId())
                .orElseThrow(() -> new BorrowerNotFoundException(req.borrowerId()));
        var book = books.getRequired(req.bookId());
        if (book.isBorrowed() || loans.findByBookIdAndReturnedAtIsNull(req.bookId()).isPresent())
            throw new BookAlreadyBorrowedException(req.bookId());

        var loan = Loan.builder().book(book).borrower(borrower).borrowedAt(Instant.now()).build();
        book.setBorrowed(true);
        loans.save(loan);
        return mapper.toResponse(loan);
    }

    @Transactional
    public LoanResponse returnBook(LoanCreateRequest req) {
        borrowers.findById(req.borrowerId())
                .orElseThrow(() -> new BorrowerNotFoundException(req.borrowerId()));
        books.findById(req.bookId())
                .orElseThrow(() -> new BookNotFoundException(req.bookId()));

        var active = loans.findByBookIdAndReturnedAtIsNull(req.bookId())
                .orElseThrow(() -> new BookAlreadyBorrowedException(req.bookId())); // reused to mean "no active loan"
        if (!active.getBorrower().getId().equals(req.borrowerId()))
            throw new IllegalStateException("This book is borrowed by a different borrower.");

        active.setReturnedAt(Instant.now());
        active.getBook().setBorrowed(false);
        return mapper.toResponse(active);
    }
}
