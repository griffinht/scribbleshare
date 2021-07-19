CREATE DATABASE scribbleshare;

\c scribbleshare

REVOKE CONNECT ON DATABASE scribbleshare FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

GRANT CONNECT ON DATABASE scribbleshare TO scribbleshare_backend;
GRANT USAGE ON SCHEMA public TO scribbleshare_backend;

GRANT CONNECT ON DATABASE scribbleshare TO scribbleshare_room;
GRANT USAGE ON SCHEMA public TO scribbleshare_room;

CREATE TABLE users(
    id bigint NOT NULL,
    owned_documents bigint[] NOT NULL,
    shared_documents bigint[] NOT NULL,
    username varchar(256) NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE ON users TO scribbleshare_backend;
GRANT SELECT, UPDATE ON users TO scribbleshare_room;

CREATE TABLE persistent_user_sessions(
      id bigint NOT NULL,
      created timestamp NOT NULL,
      expired timestamp NOT NULL,
      user_id bigint NOT NULL,
      data bytea NOT NULL,
      PRIMARY KEY (id)
);
GRANT SELECT, UPDATE, INSERT ON persistent_user_sessions TO scribbleshare_backend;

CREATE TABLE user_sessions(
     id bigint NOT NULL,
     created timestamp NOT NULL,
     expired timestamp NOT NULL,
     user_id bigint NOT NULL,
     data bytea NOT NULL,
     PRIMARY KEY (id)
);
GRANT SELECT, UPDATE, INSERT ON user_sessions TO scribbleshare_backend;
GRANT SELECT, UPDATE ON user_sessions TO scribbleshare_room;

CREATE TABLE logins(
    username varchar(256) NOT NULL,
    user_id bigint NOT NULL,
    hashed_password bytea NOT NULL,
    PRIMARY KEY (username)
);
GRANT SELECT, INSERT ON logins TO scribbleshare_backend;





CREATE TABLE documents(
    id bigint NOT NULL,
    owner bigint NOT NULL,
    name varchar(64) NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON documents TO scribbleshare_room;
GRANT SELECT ON documents TO scribbleshare_backend;

CREATE TABLE resources(
    id bigint NOT NULL,
    owner bigint NOT NULL,
    last_modified timestamp NOT NULL,
    data bytea NOT NULL,
    PRIMARY KEY (id)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON resources TO scribbleshare_room;
GRANT SELECT, INSERT ON resources TO scribbleshare_backend;

CREATE TABLE invite_codes(
    code char(6) NOT NULL,
    document bigint NOT NULL,
    PRIMARY KEY (code)
);
GRANT SELECT, INSERT, DELETE ON invite_codes TO scribbleshare_room;
