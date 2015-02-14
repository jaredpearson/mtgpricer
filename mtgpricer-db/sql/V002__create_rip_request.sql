
CREATE TABLE mtgpricer.rip_request (
	rip_request_id SERIAL PRIMARY KEY,
	start_date TIMESTAMP NOT NULL DEFAULT now(),
	finish_date TIMESTAMP
);

CREATE TABLE mtgpricer.rip_request_log (
	rip_request_log_id SERIAL PRIMARY KEY,
	rip_request_id INTEGER REFERENCES mtgpricer.rip_request (rip_request_id),
	date TIMESTAMP NOT NULL DEFAULT now(),
	value text
);