CREATE DATABASE board;

-- Connect to board database (currently on default postgres database)
\c board
REVOKE CONNECT ON DATABASE board FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

CREATE USER board_room PASSWORD 'changeme';
GRANT CONNECT ON DATABASE board TO board_room;
GRANT USAGE ON SCHEMA public TO board_room;

CREATE DOMAIN user_id AS bigint;
CREATE DOMAIN document_id AS bigint;
CREATE DOMAIN persistent_user_session_id AS bigint;
-- Create tables and grant permissions
CREATE TABLE users(
    id user_id NOT NULL,
    owned_documents document_id[] NOT NULL,
    shared_documents document_id[] NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE ON users TO board_room;

CREATE TABLE documents(
    id document_id NOT NULL,
    owner user_id NOT NULL,
    name varchar(64) not null,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE ON documents TO board_room;

CREATE TABLE canvases(
    document document_id NOT NULL,
    data bytea NOT NULL,
    PRIMARY KEY (document)
);
GRANT SELECT, INSERT, UPDATE ON canvases TO board_room;

CREATE TABLE persistent_user_sessions(
    id persistent_user_session_id NOT NULL,
    "user" user_id NOT NULL,
    creation_time timestamp,
    hashed_token bytea NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, DELETE ON persistent_user_sessions TO board_room;