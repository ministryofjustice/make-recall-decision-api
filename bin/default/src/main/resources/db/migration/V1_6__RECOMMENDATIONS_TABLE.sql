drop table if exists recommendations;

CREATE TABLE recommendations
(
    id serial primary key,
    data jsonb
);
