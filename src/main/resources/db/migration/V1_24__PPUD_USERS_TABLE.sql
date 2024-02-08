drop table if exists ppud_users;

CREATE TABLE ppud_users
(
    id                  serial primary key,
    user_name           VARCHAR(250) NOT NULL,
    ppud_user_full_name VARCHAR(250) NOT NULL,
    ppud_team_name      VARCHAR(250) NOT NULL
);

CREATE UNIQUE INDEX idx_user_name
    ON ppud_users (user_name);