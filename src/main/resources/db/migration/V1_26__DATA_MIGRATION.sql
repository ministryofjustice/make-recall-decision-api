-- finds orphan recommendations without a recommendation status and provides each with the PO_START_RECALL status
WITH RecommendationsWithoutStatuses AS (
    SELECT r.id AS recommendation_id
    FROM public.recommendations r
             LEFT JOIN public.recommendation_status rs ON r.id = rs.recommendation_id
    WHERE rs.id IS NULL
)
INSERT INTO public.recommendation_status (
    id,
    recommendation_id,
    created_by,
    created_by_user_full_name,
    created,
    modified_by,
    modified_by_user_full_name,
    modified,
    name,
    active,
    recommendation_history_id,
    email_address
)
SELECT
        (SELECT COALESCE(MAX(id), 0) FROM public.recommendation_status) + ROW_NUMBER() OVER (ORDER BY recommendation_id) AS id,
        rs.recommendation_id,
        'DATA_MIGRATION' AS created_by,
        'DATA_MIGRATION' AS created_by_user_full_name,
        CURRENT_TIMESTAMP AS created,
        NULL AS modified_by,
        NULL AS modified_by_user_full_name,
        NULL AS modified,
        'PO_START_RECALL' AS name,
        true AS active,
        NULL AS recommendation_history_id,
        NULL AS email_address
FROM RecommendationsWithoutStatuses rs;