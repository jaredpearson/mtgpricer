# MTG Pricer
Displays price information about price information for Magic: The Gathering cards from CardKingdom.com. 

# Setup
1. Download the AllSets-X.json file to `~/mtgpricer/AllSets-X.json`

    curl ~/mtgpricer/AllSets-X.json http://mtgjson.com/json/AllSets-x.json 

# Configuration
All configuration properties are specified in `src/main/resources/application.properties`. The properties can be overwritten with developer specific values by creating a new file at `src/main/resources/application-dev.properties`.

# Magic the Gathering Fan Site License
This website is not affiliated with, endorsed, sponsored, or specifically approved by Wizards of the Coast LLC. This website may use the trademarks and other intellectual property of Wizards of the Coast LLC, which is permitted under Wizards' Fan Site Policy http://company.wizards.com/fankit. For example, MAGIC: THE GATHERING® is a trademark[s] of Wizards of the Coast. For more information about Wizards of the Coast or any of Wizards' trademarks or other intellectual property, please visit their website at (www.wizards.com).

# Credits
This website contains references to the following projects

* MTGJson.com
* MTGImages.com