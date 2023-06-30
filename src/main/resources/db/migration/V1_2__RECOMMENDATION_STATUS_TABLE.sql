CREATE TABLE if not exists recommendation_status
(
    id                         serial primary key,
    recommendation_id          int,
    created_by                 VARCHAR(250),
    created_by_user_full_name  VARCHAR(250),
    created                    VARCHAR(250),
    modified_by                VARCHAR(250),
    modified_by_user_full_name VARCHAR(250),
    modified                   VARCHAR(250),
    name                       VARCHAR(250),
    active                     BOOLEAN
);
