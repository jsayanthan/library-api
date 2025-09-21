-- Adds a unique constraint to ensure a single catalog row per ISBN.
-- Multiple physical copies point to the same catalog.
ALTER TABLE book_catalog
    ADD CONSTRAINT uk_book_catalog_isbn UNIQUE (isbn);
