name "rdbms"

default_attributes(
	"postgresql" => {
		"version" => "9.3",
		"enable_pgdg_apt" => true,
		"password" => {
			"postgres" => "d!rtyDanc1ng"
		},
		"contrib" => {
			"packages" => [
				"pgcrypto"
			],
			"extensions" => [
				"pgcrypto"
			]
		},
		"config" => {
			"listen_addresses" => "*"
		},
		"pg_hba" => [
			{
				:type => 'host', 
				:db => 'all', 
				:user => 'postgres,mtgpricer_app', 
				:addr => '192.168.37.0/24', 
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