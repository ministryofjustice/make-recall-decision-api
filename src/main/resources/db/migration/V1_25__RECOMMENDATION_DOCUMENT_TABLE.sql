drop table if exists recommendation_document;

CREATE TABLE recommendation_document
(
    id                         serial primary key,
    recommendation_id          int,
    created_by                 VARCHAR(250),
    created_by_user_full_name  VARCHAR(250),
    created                    VARCHAR(250),
    uploaded_by                VARCHAR(250),
    uploaded_by_user_full_name VARCHAR(250),
    uploaded                   VARCHAR(250),
    filename                   VARCHAR(250),
    type                       VARCHAR(250),
    mimetype                   VARCHAR(250),
    data                       bytea
);


CREATE INDEX idxdocid ON recommendation_document (recommendation_id);