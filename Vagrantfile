# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.define :web do |web|
    web.vm.box = "hashicorp/precise64"
    web.vm.network "private_network", ip: "192.168.37.12"
    web.vm.provider "virtualbox" do |vb|
      vb.name = "mtgpricer-web"
      vb.memory = 2048
    end
    
    web.vm.provision :shell, inline: 'apt-get update'

    # ruby is needed to install chef
    web.vm.provision :shell, inline: 'apt-get install build-essential ruby1.9.1-dev --no-upgrade --yes'
    web.vm.provision :shell, inline: 'gem install chef --version 11.16.4 --no-rdoc --no-ri'

    web.vm.provision "chef_solo" do |chef|
      chef.cookbooks_path = ["provisioning/chef/site-cookbooks", "provisioning/chef/cookbooks"]
      chef.roles_path = "provisioning/chef/roles"
      chef.add_role "web"
      chef.add_role "solr"
    end
  end

  config.vm.define :postgres do |postgres|
    postgres.vm.box = "hashicorp/precise64"
    postgres.vm.network "private_network", ip: "192.168.37.13"
    postgres.vm.provider "virtualbox" do |vb|
      vb.name = "mtgpricer-postgres"
      vb.memory = 2048
    end
    
    postgres.vm.provision :shell, inline: 'apt-get update'

    # ruby is needed to install chef
    postgres.vm.provision :shell, inline: 'apt-get install build-essential ruby1.9.1-dev --no-upgrade --yes'
    postgres.vm.provision :shell, inline: 'gem install chef --version 11.16.4 --no-rdoc --no-ri'

    postgres.vm.provision "chef_solo" do |chef|
      chef.cookbooks_path = ["provisioning/chef/site-cookbooks", "provisioning/chef/cookbooks"]
      chef.roles_path = "provisioning/chef/roles"
      chef.add_role "rdbms"
    end
  end

end