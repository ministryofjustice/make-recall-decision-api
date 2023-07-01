alter table recommendation_status
    add column if not exists recommendation_history_id INT;