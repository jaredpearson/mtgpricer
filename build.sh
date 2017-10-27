#!/bin/bash

# check if required deps are installed
if ! [ -x "$(command -v mvn)" ]; then
    echo "Maven is not installed"
    exit 1;
fi

# download the Magic card data
if [ ! -f AllSets-x.json ]; then
    echo "Downloading AllSets-x.json"
    curl https://mtgjson.com/json/AllSets-x.json -o AllSets-x.json
else
    echo "Found AllSets-x.json. Skipping download."
fi

# build using maven
cd mtgpricer-parent
mvn package install
