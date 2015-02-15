

template '/etc/solr/solr.xml' do
  source 'solr.xml.erb'
  owner 'root'
  group 'root'
  mode '0755'
  variables(
    :port => node['mtgpricer-solr']['port']
  )
end

remote_directory '/etc/solr' do
  files_mode '0755'
  files_owner 'root'
  mode '0755'
  owner 'root'
  source 'solr'
end