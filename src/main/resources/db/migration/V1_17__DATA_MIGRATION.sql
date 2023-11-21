UPDATE recommendations r SET data = JSONB_SET(data, '{status}', '"BOOK_TO_PPUD"')
WHERE CAST(r.data ->> 'status' AS VARCHAR) IN ('CLOSED')
  AND data @> '{"recallType":{"selected":{"value":"STANDARD"}}}';

UPDATE recommendations r SET data = JSONB_SET(data, '{status}', '"BOOK_TO_PPUD"')
WHERE CAST(r.data ->> 'status' AS VARCHAR) IN ('CLOSED')
  AND data @> '{"recallType":{"selected":{"value":"FIXED"}}}';

UPDATE recommendations r SET data = JSONB_SET(data, '{status}', '"DNTR_DOWNLOADED"')
WHERE CAST(r.data ->> 'status' AS VARCHAR) IN ('CLOSED')
  AND data @> '{"recallType":{"selected":{"value":"NO_RECALL"}}}';