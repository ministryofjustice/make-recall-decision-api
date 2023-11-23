UPDATE recommendations r
SET deleted= true
WHERE CAST(r.data ->> 'status' AS VARCHAR) IN ('DELETED');

UPDATE recommendations r
SET deleted= false
WHERE CAST(r.data ->> 'status' AS VARCHAR) NOT IN ('DELETED');

ALTER TABLE ONLY recommendations ALTER COLUMN deleted SET DEFAULT false;