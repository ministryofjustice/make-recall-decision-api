drop table if exists recflowevents;

CREATE TABLE recflowevents
(
    id serial primary key,
    data jsonb
);
