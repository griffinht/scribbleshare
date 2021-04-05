CREATE DATABASE board;

-- Connect to board database (currently on default postgres database)
\c board
REVOKE CONNECT ON DATABASE board FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

CREATE USER board_room PASSWORD 'changeme';
GRANT CONNECT ON DATABASE board TO board_room;
GRANT USAGE ON SCHEMA public TO board_room;

-- Create tables and grant permissions
CREATE TABLE users(
    id bigint NOT NULL,
    owned_documents bigint[] NOT NULL,
    shared_documents bigint[] NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE ON users TO board_room;

CREATE TABLE documents(
    id bigint NOT NULL,
    owner bigint NOT NULL,
    name varchar(64) not null,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON documents TO board_room;

CREATE TABLE canvases(
    document bigint NOT NULL,
    data bytea NOT NULL,
    PRIMARY KEY (document)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON canvases TO board_room;

CREATE TABLE persistent_user_sessions(
    id bigint NOT NULL,
    "user" bigint NOT NULL,
    creation_time timestamp,
    hashed_token bytea NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, DELETE ON persistent_user_sessions TO board_room;

CREATE TABLE invite_codes(
    code char(6) NOT NULL,
    document bigint NOT NULL,
    PRIMARY KEY (code)
);
GRANT SELECT, INSERT, DELETE ON invite_codes TO board_room;