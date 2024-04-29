alter table recommendation_document
    add column if not exists document_uuid UUID UNIQUE;

CREATE INDEX idxdocuid ON recommendation_document (document_uuid);