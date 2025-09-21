package com.collabera.libraryapi.service;

import com.collabera.libraryapi.domain.dto.book.BookCreateRequest;
import com.collabera.libraryapi.domain.dto.book.BookResponse;
import com.collabera.libraryapi.domain.entity.Book;
import com.collabera.libraryapi.domain.entity.BookCatalog;
import com.collabera.libraryapi.domain.repository.BookCatalogRepository;
import com.collabera.libraryapi.domain.repository.BookRepository;
import com.collabera.libraryapi.mapper.BookMapper;
import com.collabera.libraryapi.web.exception.IsbnMetadataMismatchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookCatalogRepository catalogRepository;
    @Mock
    private BookMapper mapper;
    @InjectMocks
    private BookService service;

    static Stream<Arguments> isbnVariants() {
        return Stream.of(
                Arguments.of("978-0-13-468599-1", "9780134685991"),
                Arguments.of("978 0 13 468599 1", "9780134685991"),
                Arguments.of("  9780134685991  ", "9780134685991")
        );
    }

    @ParameterizedTest(name = "Normalizes ISBN variant {0} -> {1}")
    @MethodSource("com.collabera.libraryapi.service.BookServiceTest#isbnVariants")
    void createsNewCatalogWhenAbsent_withNormalizedIsbn(String raw, String normalized) {
        BookCreateRequest req = new BookCreateRequest(raw, "Effective Java", "Joshua Bloch");
        given(catalogRepository.findByIsbnIgnoreCase(normalized)).willReturn(Optional.empty());
        BookCatalog savedCatalog = BookCatalog.builder().isbn(normalized).title(req.title()).author(req.author()).build();
        given(catalogRepository.save(any(BookCatalog.class))).willReturn(savedCatalog);

        Book persisted = Book.builder().id(UUID.randomUUID()).catalog(savedCatalog).borrowed(false).version(0L).build();
        given(bookRepository.save(any(Book.class))).willReturn(persisted);
        BookResponse mapped = new BookResponse(persisted.getId(), normalized, req.title(), req.author(), false);
        given(mapper.toResponse(persisted)).willReturn(mapped);

        BookResponse response = service.create(req);
        ArgumentCaptor<BookCatalog> catalogCaptor = ArgumentCaptor.forClass(BookCatalog.class);
        verify(catalogRepository).save(catalogCaptor.capture());
        assertThat(catalogCaptor.getValue().getIsbn()).isEqualTo(normalized);
        assertThat(response.isbn()).isEqualTo(normalized);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void reusesExistingCatalogWhenTitleAndAuthorMatch() {
        String isbn = "9780134685991";
        BookCreateRequest req = new BookCreateRequest(isbn, "Effective Java", "Joshua Bloch");
        BookCatalog existing = BookCatalog.builder().isbn(isbn).title(req.title()).author(req.author()).build();
        given(catalogRepository.findByIsbnIgnoreCase(isbn)).willReturn(Optional.of(existing));

        Book persisted = Book.builder().id(UUID.randomUUID()).catalog(existing).borrowed(false).version(0L).build();
        given(bookRepository.save(any(Book.class))).willReturn(persisted);
        BookResponse mapped = new BookResponse(persisted.getId(), isbn, req.title(), req.author(), false);
        given(mapper.toResponse(persisted)).willReturn(mapped);

        BookResponse response = service.create(req);
        assertThat(response.id()).isEqualTo(persisted.getId());
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void throwsWhenMetadataMismatch() {
        String isbn = "9780134685991";
        BookCreateRequest req = new BookCreateRequest(isbn, "Effective Java", "Joshua Bloch");
        BookCatalog existing = BookCatalog.builder().isbn(isbn).title("Different").author("Somebody").build();
        given(catalogRepository.findByIsbnIgnoreCase(isbn)).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IsbnMetadataMismatchException.class)
                .hasMessageContaining(isbn);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void listDelegatesToRepositoryAndMaps() {
        BookCatalog cat = BookCatalog.builder().isbn("9780134685991").title("Effective Java").author("Joshua Bloch").build();
        Book book = Book.builder().id(UUID.randomUUID()).catalog(cat).borrowed(false).version(0L).build();
        Page<Book> page = new PageImpl<>(List.of(book));
        PageRequest pageable = PageRequest.of(0, 10);
        given(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), eq(pageable))).willReturn(page);
        BookResponse expected = new BookResponse(book.getId(), cat.getIsbn(), cat.getTitle(), cat.getAuthor(), false);
        given(mapper.toResponse(book)).willReturn(expected);

        Page<BookResponse> result = service.list("java", pageable);

        assertThat(result.getContent()).containsExactly(expected);
        verify(bookRepository).findAll(ArgumentMatchers.<Specification<Book>>any(), eq(pageable));
        verify(mapper).toResponse(book);
    }
}
