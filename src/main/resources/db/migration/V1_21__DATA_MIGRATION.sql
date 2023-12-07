--clean up previous migration
DELETE FROM recommendation_status WHERE recommendation_status.created_by='migration-script-ppcs';

-- add a REC_CLOSED status for existing (legacy) recommendations with both CLOSED and NO_RECALL_DECIDED or RECALL_DECIDED statuses
do
$$
    declare
        f record;
        g record;
    begin
        for f in
            (select recommendation_id
             from recommendation_status
             where name = 'NO_RECALL_DECIDED'
                or name = 'RECALL_DECIDED'
             group by recommendation_id)
            loop
                for g in
                    (select recommendation_id
                     from recommendation_status
                     where name = 'CLOSED'
                       and recommendation_id=f.recommendation_id
                     group by recommendation_id)
                    loop
                        INSERT INTO public.recommendation_status (recommendation_id, created_by, created_by_user_full_name,
                                                                  created, modified_by, modified_by_user_full_name, modified,
                                                                  name, active, recommendation_history_id, email_address)
                        VALUES (g.recommendation_id, 'migration-script-ppcs',
                                'migration-script-ppcs', now(), null, null, null, 'REC_CLOSED',
                                true, null, null);
                    end loop;
            end loop;
    end ;
$$;