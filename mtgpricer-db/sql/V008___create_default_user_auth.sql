
INSERT INTO mtgpricer.user_authorizations (
	user_id,
	authorization_name
)
SELECT user_id, 'ADMIN' 
FROM mtgpricer.users
WHERE username = 'admin@mtgpricer.com';
