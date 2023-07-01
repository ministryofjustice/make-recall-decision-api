CREATE TABLE if not exists recommendation_history
(
    id serial primary key,
    recommendation_id          int,
    modified_by                VARCHAR(250),
    modified_by_user_full_name VARCHAR(250),
    modified                   VARCHAR(250),
    recommendation jsonb
);
