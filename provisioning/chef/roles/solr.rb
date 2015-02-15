name "solr"

default_attributes(
	"solr" => {
		"install_java" => false
	}
)

run_list(
	"recipe[mtgpricer-solr::default]",
	"recipe[solr::default]"
)