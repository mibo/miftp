#!/usr/bin/env bash
#docker build --build-arg JAR_FILE=build/libs/miftp-0.1.0-SNAPSHOT.jar -t mibo/miftp -f ./scripts/Docker/Dockerfile .
# RELEASES
#docker build --build-arg JAR_FILE=build/libs/miftp-0.1.1.jar --build-arg HTTP_PORT=8081 -t mibo/miftp -t mibo/miftp:0.1.1 -f ./Dockerfile ../..
# SNAPSHOTs
docker build --build-arg JAR_FILE=build/libs/miftp-0.2.0-SNAPSHOT.jar --build-arg HTTP_PORT=8081 -t mibo/miftp:sn -f ./Dockerfile ../..