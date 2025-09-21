-- ===== Borrowers =====
CREATE TABLE borrowers (
    id         UUID         NOT NULL,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    created_by TEXT         NOT NULL DEFAULT 'system',
    updated_by TEXT         NOT NULL DEFAULT 'system',
    CONSTRAINT pk_borrowers PRIMARY KEY (id),
    CONSTRAINT uk_borrowers_email UNIQUE (email)
);

-- ===== Book Catalog (one row per ISBN) =====
CREATE TABLE book_catalog (
    isbn       VARCHAR(32)  NOT NULL,
    title      VARCHAR(255) NOT NULL,
    author     VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    created_by TEXT         NOT NULL DEFAULT 'system',
    updated_by TEXT         NOT NULL DEFAULT 'system',
    CONSTRAINT pk_book_catalog PRIMARY KEY (isbn)
);

CREATE INDEX idx_book_catalog_title  ON book_catalog(title);
CREATE INDEX idx_book_catalog_author ON book_catalog(author);

-- ===== Books (physical copies) =====
CREATE TABLE books (
    id         UUID      NOT NULL,
    catalog_isbn VARCHAR(32) NOT NULL,
    borrowed   BOOLEAN   NOT NULL DEFAULT FALSE,
    version    BIGINT    NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by TEXT      NOT NULL DEFAULT 'system',
    updated_by TEXT      NOT NULL DEFAULT 'system',
    CONSTRAINT pk_books PRIMARY KEY (id),
    CONSTRAINT fk_books_catalog
        FOREIGN KEY (catalog_isbn)
        REFERENCES book_catalog(isbn)
        ON DELETE RESTRICT,
    CONSTRAINT ck_books_version_nonneg CHECK (version >= 0)
);

CREATE INDEX idx_books_catalog ON books(catalog_isbn);

-- ===== Loans =====
CREATE TABLE loans (
    id           UUID      NOT NULL,
    book_id      UUID      NOT NULL,
    borrower_id  UUID      NOT NULL,
    borrowed_at  TIMESTAMP NOT NULL,
    returned_at  TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now(),
    created_by   TEXT      NOT NULL DEFAULT 'system',
    updated_by   TEXT      NOT NULL DEFAULT 'system',
    CONSTRAINT pk_loans PRIMARY KEY (id),
    CONSTRAINT fk_loans_book
        FOREIGN KEY (book_id)
        REFERENCES books(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_loans_borrower
        FOREIGN KEY (borrower_id)
        REFERENCES borrowers(id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_loans_dates CHECK (returned_at IS NULL OR returned_at >= borrowed_at)
);
