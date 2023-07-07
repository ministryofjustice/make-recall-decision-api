alter table recommendation_status
    add column if not exists email_address VARCHAR;