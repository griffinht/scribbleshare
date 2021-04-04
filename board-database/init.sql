CREATE USER board_room PASSWORD 'changeme';
CREATE DATABASE board;

-- Connect to board database (currently on default postgres database)
\c board
REVOKE CONNECT FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;
-- CREATE SCHEMA board;
-- GRANT USAGE ON SCHEMA board TO board_room; todo use schema and put tables on it?
GRANT CONNECT TO board_room;
GRANT USAGE TO board_room;

-- Create tables and grant permissions
CREATE TABLE users(
    id BIGINT NOT NULL,
    owned_documents BIGINT[] NOT NULL,
    shared_documents BIGINT[] NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE ON users TO board_room;

CREATE TABLE documents(
    id BIGINT NOT NULL,
    owner BIGINT NOT NULL,
    name CHAR NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE ON documents TO board_room;

CREATE TABLE canvases(
    document BIGINT NOT NULL,
    data bytea NOT NULL,
    PRIMARY KEY (document)
);
GRANT SELECT, INSERT, UPDATE ON canvases TO board_room;

CREATE TABLE persistent_user_sessions(
    id BIGINT NOT NULL,
    creation_time BIGINT NOT NULL,
    hashed_token bytea NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, DELETE ON persistent_user_sessions TO board_room;