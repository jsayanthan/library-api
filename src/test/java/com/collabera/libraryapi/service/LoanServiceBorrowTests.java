package com.collabera.libraryapi.service;

import com.collabera.libraryapi.domain.dto.loan.LoanCreateRequest;
import com.collabera.libraryapi.domain.dto.loan.LoanResponse;
import com.collabera.libraryapi.domain.entity.Book;
import com.collabera.libraryapi.domain.entity.BookCatalog;
import com.collabera.libraryapi.domain.entity.Borrower;
import com.collabera.libraryapi.domain.entity.Loan;
import com.collabera.libraryapi.domain.repository.BookRepository;
import com.collabera.libraryapi.domain.repository.BorrowerRepository;
import com.collabera.libraryapi.domain.repository.LoanRepository;
import com.collabera.libraryapi.mapper.LoanMapper;
import com.collabera.libraryapi.web.exception.BookAlreadyBorrowedException;
import com.collabera.libraryapi.web.exception.BookNotFoundException;
import com.collabera.libraryapi.web.exception.BorrowerNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceBorrowTests {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BorrowerRepository borrowerRepository;
    @Mock
    private LoanMapper mapper;
    @InjectMocks
    private LoanService service;

    private final UUID bookId = UUID.randomUUID();
    private final UUID borrowerId = UUID.randomUUID();

    @Test
    void borrowSuccess() {
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        Borrower borrower = Borrower.builder().id(borrowerId).name("Sayan").email("sayan@example.com").build();
        BookCatalog cat = BookCatalog.builder().isbn("9780134685991").title("Effective Java").author("Joshua Bloch").build();
        Book book = Book.builder().id(bookId).catalog(cat).borrowed(false).version(0L).build();
        Loan saved = Loan.builder().id(UUID.randomUUID()).book(book).borrower(borrower).borrowedAt(Instant.now()).build();

        given(borrowerRepository.findById(borrowerId)).willReturn(Optional.of(borrower));
        given(bookRepository.getRequired(bookId)).willReturn(book);
        given(bookRepository.markBorrowedIfAvailable(bookId)).willReturn(1);
        given(bookRepository.findWithCatalogById(bookId)).willReturn(Optional.of(book));
        given(loanRepository.save(any(Loan.class))).willReturn(saved);
        LoanResponse mapped = new LoanResponse(saved.getId(), null, null, saved.getBorrowedAt(), null);
        given(mapper.toResponse(saved)).willReturn(mapped);

        LoanResponse response = service.borrow(req);

        assertThat(response.id()).isEqualTo(saved.getId());
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void borrowerNotFound() {
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        given(borrowerRepository.findById(borrowerId)).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.borrow(req))
                .isInstanceOf(BorrowerNotFoundException.class);
        verify(bookRepository, never()).findById(any());
    }

    @Test
    void bookNotFound() {
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        Borrower borrower = Borrower.builder().id(borrowerId).name("Sayan").email("sayan@example.com").build();
        given(borrowerRepository.findById(borrowerId)).willReturn(Optional.of(borrower));
        given(bookRepository.getRequired(bookId)).willThrow(new BookNotFoundException(bookId));
        assertThatThrownBy(() -> service.borrow(req))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void alreadyBorrowed() {
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        Borrower borrower = Borrower.builder().id(borrowerId).name("Sayan").email("sayan@example.com").build();
        Book book = Book.builder().id(bookId).catalog(BookCatalog.builder().isbn("X").title("T").author("A").build()).borrowed(false).version(0L).build();
        given(borrowerRepository.findById(borrowerId)).willReturn(Optional.of(borrower));
        given(bookRepository.getRequired(bookId)).willReturn(book);
        given(bookRepository.markBorrowedIfAvailable(bookId)).willReturn(0);

        assertThatThrownBy(() -> service.borrow(req))
                .isInstanceOf(BookAlreadyBorrowedException.class);
        verify(loanRepository, never()).save(any());
    }
}
