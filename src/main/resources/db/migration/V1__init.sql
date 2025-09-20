-- Create BookCatalog (unique ISBN registry)
CREATE TABLE book_catalog (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL
);

-- Create Book (copies of catalog entries)
CREATE TABLE book (
    id UUID PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL,
    borrowed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_catalog FOREIGN KEY (isbn) REFERENCES book_catalog(isbn)
);

-- Create Borrower
CREATE TABLE borrower (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Loan
CREATE TABLE loan (
    id UUID PRIMARY KEY,
    book_id UUID NOT NULL,
    borrower_id UUID NOT NULL,
    borrowed_at TIMESTAMP NOT NULL,
    returned_at TIMESTAMP,
    CONSTRAINT fk_loan_book FOREIGN KEY (book_id) REFERENCES book(id),
    CONSTRAINT fk_loan_borrower FOREIGN KEY (borrower_id) REFERENCES borrower(id)
);
