CREATE INDEX idxcrn ON recommendations USING GIN ((data -> 'crn'));
CREATE INDEX idxrecid ON recommendation_status (recommendation_id);