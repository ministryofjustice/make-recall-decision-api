drop table if exists recommendations;

CREATE TABLE recommendations
(
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    crn VARCHAR NOT NULL,
    recommendation VARCHAR,
    alternate_actions VARCHAR
);