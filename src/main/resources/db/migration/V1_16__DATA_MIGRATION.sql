ALTER TABLE recommendations
ADD deleted boolean;

UPDATE recommendations r
SET deleted= true
WHERE CAST(r.data ->> 'status' AS VARCHAR) IN ('DELETED');

UPDATE recommendations r
SET deleted= false
WHERE CAST(r.data ->> 'status' AS VARCHAR) NOT IN ('DELETED');