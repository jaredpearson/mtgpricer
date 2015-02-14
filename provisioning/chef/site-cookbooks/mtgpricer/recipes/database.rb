
include_recipe 'database::postgresql'

postgresql_connection_info = {
	:host => "127.0.0.1",
	:port => node['postgresql']['config']['port'],
	:username => 'postgres',
	:password => node['postgresql']['password']['postgres']
}

postgresql_database 'mtgpricer' do
	connection postgresql_connection_info
	owner 'postgres'
	action :create
end

# create the application database user
postgresql_database_user 'mtgpricer_app' do
	connection postgresql_connection_info
	password 'badK!tty1'
	action :create
end
