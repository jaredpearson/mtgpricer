name "web"

default_attributes(
	"java" => {
		"install_flavor" => "oracle",
		"jdk_version" => "8",
		"oracle" => {
			"accept_oracle_download_terms" => true
		}
	},
	"redisio" => {
		"servers" => [
			{
				"name" => "master",
				"port" => 6379
			}
		]
	}
)

run_list(
	"recipe[apt]", 
	"recipe[java::default]", 
	"recipe[redisio]",
	"recipe[redisio::enable]",
	"recipe[mtgpricer::webapp]"
)