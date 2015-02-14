#!/bin/bash

flyway -configFile="$PWD/flyway.properties" -password='d!rtyDanc1ng' -locations="filesystem:$PWD/sql" migrate
