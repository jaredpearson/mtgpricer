
directory "Create the application root directory" do
    owner "root"
    group "root"
    mode 0755
    path "/usr/share/mtgpricer"
end

directory "Create the data directory" do
    owner "root"
    group "root"
    mode 0755
    path "/usr/share/mtgpricer/data"
end

# The set data is required for the app, so copy it to the server
file "/usr/share/mtgpricer/data/AllSets-x.json" do
    owner "root"
    group "root"
    mode 0755
    content lazy { ::File.open(node['mtgpricer']['data']['sets_json']).read }
end

# create the directory for the priceData and copy any price data
directory "Create the priceData directory" do
    owner "root"
    group "root"
    mode 0755
    path "/usr/share/mtgpricer/data/priceData"
end

Dir[ "#{node['mtgpricer']['data']['priceData']}/*" ].each do | curr_path |
    file "/usr/share/mtgpricer/data/priceData/#{Pathname.new(curr_path).basename}" do
        owner "root"
        group "root"
        mode 0755
        content lazy { ::File.open(curr_path).read }
    end if File.file?(curr_path)
end

# copy the JAR to the server
file "/usr/share/mtgpricer/mtgpricer.jar" do
    owner "root"
    group "root"
    mode 0755
    content ::File.open(node['mtgpricer']['webapp']['jar']).read
    action :create
    notifies :restart, "service[mtgpricer_webapp]"
end

# create the directory for the sessions
directory "Create the session directory" do
    owner "root"
    group "root"
    mode 0755
    path node['mtgpricer']['webapp']['sessionStoreDir']
    recursive true
    action :create
end

service "mtgpricer_webapp" do
    supports :start => true, :stop => true
    action :nothing
end
template "mtgpricer service conf" do
    path "/etc/init.d/mtgpricer_webapp"
    source "mtgpricer_webapp.service.erb"
    owner "root"
    group "root"
    mode "0755"
    notifies :enable, "service[mtgpricer_webapp]"
    notifies :start, "service[mtgpricer_webapp]"
end

# install the server helper scripts
cookbook_file '/usr/share/mtgpricer/server-stop.sh' do
    source "server-stop.sh"
    owner "root"
    group "root"
    mode "0755"
    action :create
end
template "mtgpricer service conf" do
    path "/usr/share/mtgpricer/server-start.sh"
    source "server-start.sh.erb"
    owner "root"
    group "root"
    mode "0755"
    variables(
        :port => node['mtgpricer']['webapp']['port'],
        :log => node['mtgpricer']['webapp']['log'],
        :sessionStoreDir => node['mtgpricer']['webapp']['sessionStoreDir']
    )
end