INSERT INTO ppud_users (user_name, ppud_user_full_name, ppud_team_name, ppud_user_name)
VALUES ('MAKE_RECALL_DECISION_PPCS_USER', 'car test', 'team 1', 'car_test')
ON CONFLICT (user_name)
DO UPDATE SET
	 ppud_user_full_name = 'car test',
	 ppud_team_name = 'team 1',
	 ppud_user_name = 'car_test'