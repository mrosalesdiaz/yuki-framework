#!/bin/bash
echo '#######################################################################################################################################'
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
   	CREATE USER loyalty WITH PASSWORD 'moresecure';
    CREATE DATABASE db_loyalty TEMPLATE template0;
    GRANT ALL PRIVILEGES ON DATABASE db_loyalty TO loyalty;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "db_loyalty" <<-EOSQL
   	-- register extensions
    CREATE EXTENSION "pgcrypto";
    CREATE EXTENSION "uuid-ossp";
EOSQL