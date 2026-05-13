ALTER TABLE books ADD COLUMN external_source VARCHAR(80);
ALTER TABLE books ADD COLUMN external_id VARCHAR(255);

CREATE UNIQUE INDEX uk_books_external_source_id ON books(external_source, external_id);
