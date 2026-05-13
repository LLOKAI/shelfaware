INSERT INTO books (
    title,
    authors,
    isbn,
    description,
    cover_image_url,
    publisher,
    categories,
    published_date,
    page_count,
    created_at,
    updated_at
) VALUES
(
    'Designing Data-Intensive Applications',
    'Martin Kleppmann',
    '9781449373320',
    'A practical tour through distributed systems, storage engines, stream processing, and the tradeoffs behind reliable data systems.',
    'https://covers.openlibrary.org/b/isbn/9781449373320-L.jpg',
    'O''Reilly Media',
    'Software Architecture, Databases',
    '2017-03-16',
    616,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Clean Architecture',
    'Robert C. Martin',
    '9780134494166',
    'A software design book focused on boundaries, dependency direction, and keeping business rules independent from delivery mechanisms.',
    'https://covers.openlibrary.org/b/isbn/9780134494166-L.jpg',
    'Prentice Hall',
    'Software Engineering, Architecture',
    '2017-09-10',
    432,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Effective Java',
    'Joshua Bloch',
    '9780134685991',
    'A collection of practical Java guidance covering object creation, generics, lambdas, concurrency, and API design.',
    'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg',
    'Addison-Wesley Professional',
    'Java, Software Engineering',
    '2018-01-06',
    416,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
