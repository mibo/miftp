#!/usr/bin/env bash
# Build command for Releases
docker build --build-arg JAR_FILE=build/libs/miftp-0.2.0.jar --build-arg HTTP_PORT=8081 -t mibo/rpi-miftp -t mibo/rpi-miftp:0.2.0 -f ./Dockerfile ../..
# Build command for SNAPSHOTs
#docker build --build-arg JAR_FILE=build/libs/miftp-0.2.0-SNAPSHOT.jar --build-arg HTTP_PORT=8081 -t mibo/rpi-miftp:sn -f ./Dockerfile ../..
