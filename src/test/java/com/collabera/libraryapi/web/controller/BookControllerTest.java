package com.collabera.libraryapi.web.controller;

import com.collabera.libraryapi.domain.dto.book.BookCreateRequest;
import com.collabera.libraryapi.domain.dto.book.BookResponse;
import com.collabera.libraryapi.domain.repository.BookCatalogRepository;
import com.collabera.libraryapi.service.BookService;
import com.collabera.libraryapi.web.exception.ApiError;
import com.collabera.libraryapi.web.exception.IsbnMetadataMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest extends BaseMockMvcTest {
    @MockBean
    private BookService bookService;

    private static Stream<Arguments> invalidCreatePayloads() {
        return Stream.of(
                Arguments.of(new BookCreateRequest("", "Title", "Author"), "isbn: ISBN cannot be blank"),
                Arguments.of(new BookCreateRequest("BAD", "Title", "Author"), "isbn: Invalid ISBN-10/13 format"),
                Arguments.of(new BookCreateRequest("9780134685991", "", "Author"), "title: Title cannot be blank"),
                Arguments.of(new BookCreateRequest("9780134685991", "Title", ""), "author: Author cannot be blank")
        );
    }

    @Test
    void create_201() throws Exception {
        UUID id = UUID.randomUUID();
        BookCreateRequest req = new BookCreateRequest("978-0-13-468599-1", "Effective Java", "Joshua Bloch");
        BookResponse resp = new BookResponse(id, "9780134685991", req.title(), req.author(), false);
    given(bookService.create(any())).willReturn(resp);

        MvcResult result = mockMvc.perform(post("/api/v1/books")
                        .contentType(json()).content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn();
        BookResponse actual = fromJson(result.getResponse().getContentAsString(), BookResponse.class);
        assertThat(actual).usingRecursiveComparison().isEqualTo(resp);
    }

    @ParameterizedTest
    @MethodSource("invalidCreatePayloads")
    void create_400_validation(BookCreateRequest req, String expectedMsgPart) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/books")
                        .contentType(json()).content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.message()).contains(expectedMsgPart);
    }

    @Test
    @DisplayName("create_409_isbnMetadataMismatch")
    void create_409_isbnMetadataMismatch() throws Exception {
        BookCreateRequest req = new BookCreateRequest("9780134685991", "Effective Java", "Some Author");
    given(bookService.create(any())).willThrow(new IsbnMetadataMismatchException("9780134685991"));
        MvcResult result = mockMvc.perform(post("/api/v1/books")
                        .contentType(json()).content(toJson(req)))
                .andExpect(status().isConflict())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.code()).isEqualTo("ISBN_METADATA_MISMATCH");
    }

    @Test
    void list_200_withSearchAndPaging() throws Exception {
        UUID id = UUID.randomUUID();
        BookResponse item = new BookResponse(id, "9780134685991", "Effective Java", "Joshua Bloch", false);
        Page<BookResponse> page = new PageImpl<>(List.of(item), PageRequest.of(0, 1), 1);
    given(bookService.list(eq("java"), any())).willReturn(page);

        MvcResult result = mockMvc.perform(get("/api/v1/books?search=java&page=0&size=1"))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        assertThat(json).contains("\"totalElements\":1").contains("Effective Java");
    }

    @Test
    @DisplayName("create_201_secondCopySameIsbn")
    void create_201_secondCopySameIsbn() throws Exception {
        // Simulate service creating another physical copy (different internal id)
        UUID id = UUID.randomUUID();
        BookCreateRequest req = new BookCreateRequest("978-0-13-468599-1", "Effective Java", "Joshua Bloch");
        BookResponse resp = new BookResponse(id, "9780134685991", req.title(), req.author(), false);
        given(bookService.create(any())).willReturn(resp);
        MvcResult result = mockMvc.perform(post("/api/v1/books")
                        .contentType(json())
                        .content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn();
        BookResponse actual = fromJson(result.getResponse().getContentAsString(), BookResponse.class);
        assertThat(actual.isbn()).isEqualTo("9780134685991");
    }
}
