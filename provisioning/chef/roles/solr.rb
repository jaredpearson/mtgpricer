name "solr"

default_attributes(
	"solr" => {
		"install_java" => false,
		"version" => "4.10.4",
		"checksum" => "ac3543880f1b591bcaa962d7508b528d7b42e2b5548386197940b704629ae851"
	}
)

run_list(
	"recipe[mtgpricer-solr::default]",
	"recipe[solr::default]"
)