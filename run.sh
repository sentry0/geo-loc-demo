#!/usr/bin/env bash

# Copyright 2020-Present Philip J. Guinchard
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#        http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

PROFILE="local"

POSTGIS_PREFIX="geoloc"

CONTAINER_NAME=${POSTGIS_PREFIX}demo-postgis

REGEX="^${POSTGIS_PREFIX}demo-postgis$"

PARAMS=""

INSTALL=0

RUN_TESTS=0

TESTS_ONLY=0

while (( "$#" )); do
  case "$1" in
    -i|--install)
      INSTALL=1
      shift
      ;;
    -t|--tests)
      RUN_TESTS=1
      shift
      ;;
    -T|--test-only)
      TESTS_ONLY=1
      shift
      ;;
    -*|--*=) # unsupported flags
      echo "Error: Unsupported flag $1" >&2
      exit 1
      ;;
    *) # preserve positional arguments
      PARAMS="$PARAMS $1"
      shift
      ;;
  esac
done

# set positional arguments in their proper place
eval set -- "$PARAMS"

if ! docker ps -a --format '{{.Names}}' | egrep $REGEX &> /dev/null; then
    docker run --name $CONTAINER_NAME \
        -p 6501:5432 \
        -e POSTGRES_USER=$POSTGIS_PREFIX \
        -e POSTGRES_PASSWORD=$POSTGIS_PREFIX \
        -d \
        postgis/postgis
elif docker ps -a -f name=$CONTAINER_NAME --format '{{.Status}}' | egrep '^Up.*\(Paused\)$' &> /dev/null; then
    docker unpause $CONTAINER_NAME
elif ! docker ps -a -f name=$CONTAINER_NAME --format '{{.Status}}' | egrep '^Up.*$' &> /dev/null; then
    docker restart $CONTAINER_NAME
fi

if [ ! -d "target" ] || [ -z "$(ls -A target)" ] || [ $INSTALL == 1 ]; then
    ./mvnw clean install -DskipTests
fi

if [ $RUN_TESTS == 1 ] || [ $TESTS_ONLY == 1 ]; then
    ./mvnw test -Dspring.profiles.active=$PROFILE
fi

if [ $TESTS_ONLY == 0 ]; then
    if lsof -i -P -n | egrep '\*:8080 \(LISTEN\)' &> /dev/null; then
        echo "Something is already listening on port 8080"
    else
        ./mvnw spring-boot:run -Dspring-boot.run.profiles=$PROFILE
    fi
fi