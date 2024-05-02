alter table recommendation_document
    add column if not exists title VARCHAR(250);
