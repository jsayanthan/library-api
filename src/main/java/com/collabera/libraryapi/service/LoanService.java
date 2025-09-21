package com.collabera.libraryapi.service;

import com.collabera.libraryapi.domain.entity.Loan;
import com.collabera.libraryapi.domain.repository.BookRepository;
import com.collabera.libraryapi.domain.repository.BorrowerRepository;
import com.collabera.libraryapi.domain.repository.LoanRepository;
import com.collabera.libraryapi.domain.dto.loan.LoanCreateRequest;
import com.collabera.libraryapi.domain.dto.loan.LoanResponse;
import com.collabera.libraryapi.mapper.LoanMapper;
import com.collabera.libraryapi.web.exception.*;
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
        // Atomic update; succeed only if flipped false->true
        if (books.markBorrowedIfAvailable(book.getId()) == 0) {
            throw new BookAlreadyBorrowedException(req.bookId());
        }
        book = books.findWithCatalogById(book.getId()).orElse(book);

        var loan = Loan.builder().book(book).borrower(borrower).borrowedAt(Instant.now()).build();
        return mapper.toResponse(loans.save(loan));
    }

    @Transactional
    public LoanResponse returnBook(LoanCreateRequest req) {
        borrowers.findById(req.borrowerId())
                .orElseThrow(() -> new BorrowerNotFoundException(req.borrowerId()));
        books.findById(req.bookId())
                .orElseThrow(() -> new BookNotFoundException(req.bookId()));
        var active = loans.findByBookIdAndReturnedAtIsNull(req.bookId())
                .orElseThrow(() -> new ActiveLoanNotFoundException(req.bookId()));
        if (!active.getBorrower().getId().equals(req.borrowerId())) {
            throw new WrongBorrowerException(req.bookId());
        }
        active.setReturnedAt(Instant.now());
        loans.save(active);
        books.markReturned(req.bookId());
        active.setBook(books.findWithCatalogById(req.bookId()).orElse(active.getBook()));
        return mapper.toResponse(active);
    }
}
