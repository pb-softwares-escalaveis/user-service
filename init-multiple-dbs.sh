#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "user" --dbname "userservicedb" <<-EOSQL
    CREATE DATABASE keycloakdb;
    GRANT ALL PRIVILEGES ON DATABASE keycloakdb TO "user";
EOSQL