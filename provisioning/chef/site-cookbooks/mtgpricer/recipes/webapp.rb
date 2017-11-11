
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
    variables({
        log: node['mtgpricer']['webapp']['log']
    })
    notifies :enable, "service[mtgpricer_webapp]"
    notifies :start, "service[mtgpricer_webapp]"
end
