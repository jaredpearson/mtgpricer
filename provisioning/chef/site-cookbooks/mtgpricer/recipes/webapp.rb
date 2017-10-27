
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
    content ::File.open(node['mtgpricer']['data']['sets_json']).read
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
