package com.collabera.libraryapi.web.controller;

import com.collabera.libraryapi.domain.dto.book.BookResponse;
import com.collabera.libraryapi.domain.dto.borrower.BorrowerResponse;
import com.collabera.libraryapi.domain.dto.loan.LoanCreateRequest;
import com.collabera.libraryapi.domain.dto.loan.LoanResponse;
import com.collabera.libraryapi.service.LoanService;
import com.collabera.libraryapi.web.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerTest extends BaseMockMvcTest {
        @MockBean
        private LoanService loanService;

    private static Stream<Arguments> invalidCreate() {
        return Stream.of(
                Arguments.of(new LoanCreateRequest(null, UUID.randomUUID()), "bookId: Book ID is required"),
                Arguments.of(new LoanCreateRequest(UUID.randomUUID(), null), "borrowerId: Borrower ID is required")
        );
    }

    @Test
    void borrow_201() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        LoanResponse resp = new LoanResponse(UUID.randomUUID(),
                new BookResponse(bookId, "9780134685991", "Effective Java", "Joshua Bloch", true),
                new BorrowerResponse(borrowerId, "Sayan", "sayan@example.com"), Instant.now(), null);
        given(loanService.borrow(any())).willReturn(resp);
        MvcResult result = mockMvc.perform(post("/api/v1/loans").contentType(json()).content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn();
        LoanResponse actual = fromJson(result.getResponse().getContentAsString(), LoanResponse.class);
        assertThat(actual.book().isbn()).isEqualTo("9780134685991");
    }

    @ParameterizedTest
    @MethodSource("invalidCreate")
    void borrow_400_validation(LoanCreateRequest req, String expectedPart) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/loans").contentType(json()).content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.message()).contains(expectedPart);
    }

    @Test
    void borrow_404_borrowerNotFound() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        given(loanService.borrow(any())).willThrow(new BorrowerNotFoundException(borrowerId));
        MvcResult result = mockMvc.perform(post("/api/v1/loans").contentType(json()).content(toJson(req)))
                .andExpect(status().isNotFound())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.code()).isEqualTo("BORROWER_NOT_FOUND");
    }

    @Test
    void borrow_404_bookNotFound() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        given(loanService.borrow(any())).willThrow(new BookNotFoundException(bookId));
        MvcResult result = mockMvc.perform(post("/api/v1/loans").contentType(json()).content(toJson(req)))
                .andExpect(status().isNotFound())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.code()).isEqualTo("BOOK_NOT_FOUND");
    }

    @Test
    void borrow_409_alreadyBorrowed() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        given(loanService.borrow(any())).willThrow(new BookAlreadyBorrowedException(bookId));
        MvcResult result = mockMvc.perform(post("/api/v1/loans").contentType(json()).content(toJson(req)))
                .andExpect(status().isConflict())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.code()).isEqualTo("BOOK_ALREADY_BORROWED");
    }

    // Return tests
    private static Stream<Arguments> invalidReturn() { return invalidCreate(); }

    @Test
    void return_200() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        LoanResponse resp = new LoanResponse(UUID.randomUUID(),
                new BookResponse(bookId, "9780134685991", "Effective Java", "Joshua Bloch", false),
                new BorrowerResponse(borrowerId, "Sayan", "sayan@example.com"), Instant.now(), Instant.now());
        given(loanService.returnBook(any())).willReturn(resp);
        MvcResult result = mockMvc.perform(post("/api/v1/loans/return").contentType(json()).content(toJson(req)))
                .andExpect(status().isOk())
                .andReturn();
        LoanResponse actual = fromJson(result.getResponse().getContentAsString(), LoanResponse.class);
        assertThat(actual.returnedAt()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("invalidReturn")
    void return_400_validation(LoanCreateRequest req, String expectedPart) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/loans/return").contentType(json()).content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.message()).contains(expectedPart);
    }

    @Test
    void return_404_noActiveLoan() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        given(loanService.returnBook(any())).willThrow(new ActiveLoanNotFoundException(bookId));
        MvcResult result = mockMvc.perform(post("/api/v1/loans/return").contentType(json()).content(toJson(req)))
                .andExpect(status().isNotFound())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.code()).isEqualTo("ACTIVE_LOAN_NOT_FOUND");
    }

    @Test
    void return_409_wrongBorrower() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID borrowerId = UUID.randomUUID();
        LoanCreateRequest req = new LoanCreateRequest(bookId, borrowerId);
        given(loanService.returnBook(any())).willThrow(new WrongBorrowerException(bookId));
        MvcResult result = mockMvc.perform(post("/api/v1/loans/return").contentType(json()).content(toJson(req)))
                .andExpect(status().isConflict())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.code()).isEqualTo("WRONG_BORROWER");
    }
}
