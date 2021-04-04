CREATE USER board PASSWORD 'changeme';
CREATE DATABASE board;

-- Connect to board database (currently on default postgres database)
\c board
CREATE TABLE users(
    id bigint NOT NULL,
    owned_documents bytea[] NOT NULL,
    shared_documents bytea[] NOT NULL,
    PRIMARY KEY(id)
)