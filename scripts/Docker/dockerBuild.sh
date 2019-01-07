#!/usr/bin/env bash
# RELEASES
#docker build --build-arg JAR_FILE=build/libs/miftp-0.2.0.jar --build-arg HTTP_PORT=8081 -t mibo/miftp -t mibo/miftp:0.2.0 -f ./Dockerfile ../..
# SNAPSHOTs
docker build --build-arg JAR_FILE=build/libs/miftp-0.3.0-SNAPSHOT.jar --build-arg HTTP_PORT=8081 -t mibo/miftp:sn -f ./Dockerfile ../..