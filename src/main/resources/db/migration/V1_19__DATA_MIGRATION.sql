-- add a DNTR_DOWNLOADED status for existing (legacy) recommendations with both CLOSED and NO_RECALL_DECIDED statuses
do
$$
    declare
        f record;
    begin
        for f in
            (select recommendation_id
             from recommendation_status
             where name = 'NO_RECALL_DECIDED'
             group by recommendation_id)
            loop
                INSERT INTO public.recommendation_status (recommendation_id, created_by, created_by_user_full_name,
                                                          created, modified_by, modified_by_user_full_name, modified,
                                                          name, active, recommendation_history_id, email_address)
                VALUES (f.recommendation_id, 'migration-script-ppcs',
                        'migration-script-ppcs', now(), null, null, null, 'DNTR_DOWNLOADED',
                        true, null, null);
            end loop;
    end ;
$$;

-- add a BOOK_TO_PPUD status for existing (legacy) recommendations with both CLOSED and RECALL_DECIDED statuses
do
$$
    declare
        f record;
    begin
        for f in
            (select recommendation_id
             from recommendation_status
             where name = 'RECALL_DECIDED'
             group by recommendation_id)
            loop
                INSERT INTO public.recommendation_status (recommendation_id, created_by, created_by_user_full_name,
                                                          created, modified_by, modified_by_user_full_name, modified,
                                                          name, active, recommendation_history_id, email_address)
                VALUES (f.recommendation_id, 'migration-script-ppcs',
                        'migration-script-ppcs', now(), null, null, null, 'BOOK_TO_PPUD',
                        true, null, null);
            end loop;
    end ;
$$;