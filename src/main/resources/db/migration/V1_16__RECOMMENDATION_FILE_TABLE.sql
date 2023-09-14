drop table if exists recommendation_file;

CREATE TABLE recommendation_file
(
    id                         serial primary key,
    recommendation_id          int,
    created_by                 VARCHAR(250),
    created_by_user_full_name  VARCHAR(250),
    created                    VARCHAR(250),
    name                       VARCHAR(250),
    token                      VARCHAR(250),
    s3_id                      VARCHAR(250),
    category                   VARCHAR(250),
    type                       VARCHAR(250),
    notes                      VARCHAR,
    size                       VARCHAR(250)
);