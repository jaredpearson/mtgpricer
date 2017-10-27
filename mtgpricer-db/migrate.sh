#!/bin/bash

# check if required deps are installed
if ! [ -x "$(command -v flyway)" ]; then
    echo "Flyway is not installed"
    exit 1;
fi

flyway -configFile="$PWD/flyway.properties" -password='d!rtyDanc1ng' -locations="filesystem:$PWD/sql" migrate
