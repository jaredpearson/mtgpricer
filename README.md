# MTG Pricer
Displays price information about price information for Magic: The Gathering cards from CardKingdom.com. 

The project is broken down into two modules
* `mtgpricer-rip` contains all of the ripping and processing of the price information
* `mtgpricer-web` contains the web UI

## Setup

In order to run the MTG Pricer server, you will need the following:

* JDK 8
* Maven 3.2.2
* Vagrant 1.9

Build the Java application:
```
$ sh build.sh
```

Create the VM's with Vagrant:
```
$ vagrant up
```

After the servers start, the application should be accessible at [http://192.168.37.12:8080/](http://192.168.37.12:8080/).

After making changes to the web application, build the source and provision the server with the compiled assets using:
```
$ sh build.sh && vagrant up web
```

### Database

See the [database readme](mtgpricer-db/README.md) for information on the database.

### Logs

The web server will automatically write information to the logs within the VM. To see the logs:

```
$ vagrant ssh web
$ tail -n 200 -f /var/log/mtgpricer.log
```

## Magic the Gathering Fan Site License
This website is not affiliated with, endorsed, sponsored, or specifically approved by Wizards of the Coast LLC. This website may use the trademarks and other intellectual property of Wizards of the Coast LLC, which is permitted under Wizards' Fan Site Policy http://company.wizards.com/fankit. For example, MAGIC: THE GATHERING® is a trademark[s] of Wizards of the Coast. For more information about Wizards of the Coast or any of Wizards' trademarks or other intellectual property, please visit their website at (www.wizards.com).

## Credits
This website contains references to the following projects

* MTGJson.com
* MTGImages.com