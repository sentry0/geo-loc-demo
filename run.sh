#!/usr/bin/env bash

if ! docker ps --format '{{.Names}}' | egrep '^geolocdemo-postgis$' &> /dev/null; then
    docker run --name geolocdemo-postgis \
        -p 6501:5432 \
        -e POSTGRES_USER=geoloc \
        -e POSTGRES_PASSWORD=geoloc \
        -d \
        postgis/postgis
fi

./mvnw clean install -Dspring.profiles.active=local
 
./mvnw spring-boot:run -Dspring-boot.run.profiles=local