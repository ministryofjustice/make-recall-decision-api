-- 1. if no associated statuses and in terminal state eg/DOCUMENT_DOWNLOADED/DELETED, write CLOSED
CREATE TEMP TABLE terminal_state_recommendations_tmp AS
SELECT *
FROM recommendation_status
LIMIT 0;
ALTER TABLE terminal_state_recommendations_tmp
    ADD crn VARCHAR;
ALTER TABLE terminal_state_recommendations_tmp
    ADD decision VARCHAR;
INSERT INTO terminal_state_recommendations_tmp (id, recommendation_id, crn, decision, created, modified_by, modified,
                                                name, created_by)
SELECT (id - (SELECT random() * 999 + 1 AS RAND_1_11)),
       id,
       data::json -> 'crn' #>> '{}',
       data::json -> 'recallType' -> 'selected' -> 'value' #>> '{}',
       data::json -> 'createdDate' #>> '{}',
       data::json -> 'lastModifiedBy' #>> '{}',
       data::json -> 'lastModifiedDate' #>> '{}',
       data::json -> 'status' #>> '{}',
       data::json -> 'createdBy' #>> '{}'
FROM make_recall_decision.public.recommendations r
WHERE CAST(r.data ->> 'status' AS VARCHAR) IN ('DOCUMENT_DOWNLOADED', 'DELETED');
ALTER TABLE terminal_state_recommendations_tmp
    DROP COLUMN crn;
UPDATE terminal_state_recommendations_tmp m
SET active= true,
    modified=null,
    modified_by=null,
    created_by='migration-script',
    created_by_user_full_name='migration-script'
WHERE m.id IS NOT NULL;
CREATE TEMP TABLE missing_terminal_recommendations_tmp AS
SELECT *
FROM terminal_state_recommendations_tmp
LIMIT 0;
INSERT INTO missing_terminal_recommendations_tmp
SELECT A.*
FROM terminal_state_recommendations_tmp A
where A.recommendation_id not in
      (select B.recommendation_id from recommendation_status B where B.recommendation_id = A.recommendation_id);
CREATE TEMP TABLE missing_terminal_recommendations_without_decision_tmp AS
SELECT *
FROM missing_terminal_recommendations_tmp;
ALTER TABLE
    missing_terminal_recommendations_without_decision_tmp
    DROP decision;
select *
from missing_terminal_recommendations_without_decision_tmp;
CREATE TEMP TABLE recommendations_to_close_tmp AS
SELECT *
FROM missing_terminal_recommendations_without_decision_tmp;
UPDATE recommendations_to_close_tmp m
SET name='CLOSED'
WHERE m.id IS NOT NULL;
INSERT INTO recommendation_status
SELECT *
FROM recommendations_to_close_tmp;

-- 2. if rec doc.status is DOCUMENT_DOWNLOADED write extra PP_DOCUMENT_CREATED status
CREATE TEMP TABLE pp_document_created_recommendations_tmp AS
SELECT *
FROM recommendation_status
LIMIT 0;
INSERT INTO pp_document_created_recommendations_tmp
SELECT *
FROM missing_terminal_recommendations_without_decision_tmp
where name = 'DOCUMENT_DOWNLOADED';
UPDATE pp_document_created_recommendations_tmp m
SET id=(id - (SELECT random() * 999 + 1 AS RAND_1_11)),
    name='PP_DOCUMENT_CREATED'
WHERE m.id IS NOT NULL;
select *
from pp_document_created_recommendations_tmp;
INSERT INTO recommendation_status
SELECT *
FROM pp_document_created_recommendations_tmp;

-- 3. if recall type is RECALL write RECALL_DECIDED to status table and likewise for NO_RECALL_DECIDED
CREATE TEMP TABLE recalls_tmp AS
SELECT *
FROM recommendation_status
LIMIT 0;
alter table recalls_tmp
    add column decision varchar;
insert into recalls_tmp(select *
                        from missing_terminal_recommendations_tmp t
                        where t.name = 'DOCUMENT_DOWNLOADED' AND t.decision != 'NO_RECALL');
alter table recalls_tmp
    drop decision;
UPDATE recalls_tmp m
SET id=(id - (SELECT random() * 999 + 1 AS RAND_1_11)),
    name='RECALL_DECIDED'
WHERE m.id IS NOT NULL;
insert into recommendation_status
select *
from recalls_tmp;

CREATE TEMP TABLE no_recalls_tmp AS
SELECT *
FROM recommendation_status
LIMIT 0;
alter table no_recalls_tmp
    add column decision varchar;
insert into no_recalls_tmp(select *
                           from missing_terminal_recommendations_tmp t
                           where t.name = 'DOCUMENT_DOWNLOADED' AND t.decision = 'NO_RECALL');
alter table no_recalls_tmp
    drop decision;
UPDATE no_recalls_tmp m
SET id=(id - (SELECT random() * 999 + 1 AS RAND_1_11)),
    name='NO_RECALL_DECIDED'
WHERE m.id IS NOT NULL;
insert into recommendation_status
select *
from no_recalls_tmp;
