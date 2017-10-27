name "rdbms"

override_attributes(
	"postgresql" => {
		"version" => "9.5",
		"dir" => "/etc/postgresql/9.5/main",
		"password" => {
			"postgres" => "d!rtyDanc1ng"
		},
		"client" => {
			"packages" => [
				"postgresql-client-9.5",
				"libpq-dev"
			]
		},
		"server" => {
			"service_name" => "postgresql",
			"packages" => [
				"postgresql-9.5"
			]
		},
		"contrib" => {
			"packages" => [
				"postgresql-contrib-9.5",
				"pgcrypto"
			],
			"extensions" => [
				"pgcrypto"
			]
		},
		"config" => {
			"listen_addresses" => "*",
			"port" => 5432
		},
		"pg_hba" => [
			{
				:type => 'host', 
				:db => 'all', 
				:user => 'postgres,mtgpricer_app', 
				:addr => '192.168.37.0/24', 
				:method => 'password'
			},
			{
				:type => 'host', 
				:db => 'all', 
				:user => 'all', 
				:addr => '0.0.0.0/0', 
				:method => 'password'
			}
		]
	}
)

run_list(
	"recipe[apt]", 
	"recipe[postgresql]", 
	"recipe[postgresql::server]",
	"recipe[mtgpricer::database]"
)