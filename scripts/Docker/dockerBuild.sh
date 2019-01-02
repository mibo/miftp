#!/usr/bin/env bash
#docker build --build-arg JAR_FILE=build/libs/miftp-0.1.0-SNAPSHOT.jar -t mibo/miftp -f ./scripts/Docker/Dockerfile .
docker build --build-arg JAR_FILE=build/libs/miftp-0.1.0-SNAPSHOT.jar --build-arg HTTP_PORT=8081 -t mibo/miftp -f ./Dockerfile ../..
