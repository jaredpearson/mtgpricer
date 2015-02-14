
The DB project uses [Flyway](http://flywaydb.org/) to execute the migration scripts into the database.

# Installation
1. Download the latest command line binary for [Flyway](http://flywaydb.org/)
2. Download the [Postgresql JDBC Driver](https://jdbc.postgresql.org/download.html) and unzip the JAR file to the `flyway/jars` directory
3. Run the `migrate.sh` script from the project root to update the database to the latest version