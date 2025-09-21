package com.collabera.libraryapi.service;

import com.collabera.libraryapi.domain.dto.loan.LoanCreateRequest;
import com.collabera.libraryapi.domain.dto.loan.LoanResponse;
import com.collabera.libraryapi.domain.entity.*;
import com.collabera.libraryapi.domain.repository.*;
import com.collabera.libraryapi.mapper.LoanMapper;
import com.collabera.libraryapi.web.exception.*;
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
// No ArgumentMatchers needed explicitly in this class
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceReturnTests {

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
    private final UUID otherBorrowerId = UUID.randomUUID();

    @Test
    void returnSuccess() {
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        Borrower borrower = Borrower.builder().id(borrowerId).name("Sayan").email("sayan@example.com").build();
        BookCatalog catalog = BookCatalog.builder().isbn("9780134685991").title("Effective Java").author("Joshua Bloch").build();
        Book book = Book.builder().id(bookId).catalog(catalog).borrowed(true).version(0L).build();
        Loan active = Loan.builder().id(UUID.randomUUID()).book(book).borrower(borrower).borrowedAt(Instant.now().minusSeconds(3600)).returnedAt(null).build();

        given(borrowerRepository.findById(borrowerId)).willReturn(Optional.of(borrower));
        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(loanRepository.findByBookIdAndReturnedAtIsNull(bookId)).willReturn(Optional.of(active));
        given(bookRepository.findWithCatalogById(bookId)).willReturn(Optional.of(book));
        given(mapper.toResponse(active)).willAnswer(inv -> {
            Loan l = inv.getArgument(0);
            return new LoanResponse(l.getId(), null, null, l.getBorrowedAt(), l.getReturnedAt());
        });

        LoanResponse resp = service.returnBook(req);
        assertThat(resp.returnedAt()).isNotNull();
        verify(bookRepository).markReturned(bookId);
    }

    @Test
    void borrowerNotFound() {
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        given(borrowerRepository.findById(borrowerId)).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.returnBook(req)).isInstanceOf(BorrowerNotFoundException.class);
    }

    @Test
    void bookNotFound() {
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        Borrower borrower = Borrower.builder().id(borrowerId).name("Sayan").email("sayan@example.com").build();
        given(borrowerRepository.findById(borrowerId)).willReturn(Optional.of(borrower));
        given(bookRepository.findById(bookId)).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.returnBook(req)).isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void noActiveLoan() {
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        Borrower borrower = Borrower.builder().id(borrowerId).name("Sayan").email("sayan@example.com").build();
        Book book = Book.builder().id(bookId).catalog(BookCatalog.builder().isbn("X").title("T").author("A").build()).borrowed(true).version(0L).build();
        given(borrowerRepository.findById(borrowerId)).willReturn(Optional.of(borrower));
        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(loanRepository.findByBookIdAndReturnedAtIsNull(bookId)).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.returnBook(req)).isInstanceOf(ActiveLoanNotFoundException.class);
    }

    @Test
    void wrongBorrower() {
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        Borrower borrower = Borrower.builder().id(borrowerId).name("Sayan").email("sayan@example.com").build();
        Borrower other = Borrower.builder().id(otherBorrowerId).name("Other").email("other@example.com").build();
        Book book = Book.builder().id(bookId).catalog(BookCatalog.builder().isbn("X").title("T").author("A").build()).borrowed(true).version(0L).build();
        Loan active = Loan.builder().id(UUID.randomUUID()).book(book).borrower(other).borrowedAt(Instant.now()).build();

        given(borrowerRepository.findById(borrowerId)).willReturn(Optional.of(borrower));
        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(loanRepository.findByBookIdAndReturnedAtIsNull(bookId)).willReturn(Optional.of(active));

        assertThatThrownBy(() -> service.returnBook(req)).isInstanceOf(WrongBorrowerException.class);
    }
}
