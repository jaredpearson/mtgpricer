
CREATE TABLE mtgpricer.users (
	user_id SERIAL PRIMARY KEY,
	username TEXT NOT NULL,
	email TEXT NOT NULL,
	password TEXT
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE mtgpricer.users TO mtgpricer_app;

CREATE TABLE mtgpricer.user_authorizations (
	user_authorization_id SERIAL PRIMARY KEY,
	user_id INTEGER NOT NULL REFERENCES mtgpricer.users(user_id),
	authorization_name TEXT NOT NULL,
	UNIQUE(user_id, authorization_name)
);

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE mtgpricer.user_authorizations TO mtgpricer_app;
