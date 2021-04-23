CREATE DATABASE key_value;

\c key_value

REVOKE CONNECT ON DATABASE key_value FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

GRANT CONNECT ON DATABASE key_value TO scribbleshare_backend;
GRANT USAGE ON SCHEMA public TO scribbleshare_backend;

GRANT CONNECT ON DATABASE key_value TO scribbleshare_room;
GRANT USAGE ON SCHEMA public TO scribbleshare_room;

CREATE TABLE key_value(
    key bigint NOT NULL,
    value bytea NOT NULL,
    PRIMARY KEY (key)
);
GRANT SELECT, INSERT ON key_value TO scribbleshare_backend;
GRANT SELECT ON key_value TO scribbleshare_room;
