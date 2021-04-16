CREATE DATABASE scribbleshare;

-- Connect to scribbleshare database (currently on default postgres database)
\c scribbleshare
REVOKE CONNECT ON DATABASE scribbleshare FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

CREATE USER scribbleshare_backend PASSWORD 'changeme';
GRANT CONNECT ON DATABASE scribbleshare TO scribbleshare_backend;
GRANT USAGE ON SCHEMA public TO scribbleshare_backend;

CREATE USER scribbleshare_room PASSWORD 'changeme';
GRANT CONNECT ON DATABASE scribbleshare TO scribbleshare_room;
GRANT USAGE ON SCHEMA public TO scribbleshare_room;

-- Create tables and grant permissions
CREATE TABLE persistent_user_sessions(
     id bigint NOT NULL,
     "user" bigint NOT NULL,
     creation_time timestamp,
     hashed_token bytea NOT NULL,
     PRIMARY KEY (id)
);
GRANT SELECT, INSERT, DELETE ON persistent_user_sessions TO scribbleshare_backend;

CREATE TABLE users(
    id bigint NOT NULL,
    owned_documents bigint[] NOT NULL,
    shared_documents bigint[] NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE ON users TO scribbleshare_backend;
GRANT SELECT, UPDATE ON users TO scribbleshare_room;

CREATE TABLE documents(
    id bigint NOT NULL,
    owner bigint NOT NULL,
    name varchar(64) not null,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON documents TO scribbleshare_room;

CREATE TABLE canvases(
    document bigint NOT NULL,
    data bytea NOT NULL,
    PRIMARY KEY (document)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON canvases TO scribbleshare_room;

CREATE TABLE invite_codes(
    code char(6) NOT NULL,
    document bigint NOT NULL,
    PRIMARY KEY (code)
);
GRANT SELECT, INSERT, DELETE ON invite_codes TO scribbleshare_room;