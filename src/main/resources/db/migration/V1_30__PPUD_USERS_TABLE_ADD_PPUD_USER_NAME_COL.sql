ALTER TABLE ONLY ppud_users
    DROP COLUMN IF EXISTS ppud_user_name;

ALTER TABLE ONLY ppud_users
    ADD ppud_user_name VARCHAR(512) DEFAULT '<not set>' NOT NULL;