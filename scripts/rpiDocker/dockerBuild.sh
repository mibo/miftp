#!/usr/bin/env bash
#docker build --build-arg JAR_FILE=build/libs/miftp-0.1.1.jar --build-arg HTTP_PORT=8081 -t mibo/rpiMiftp -t mibo/miftp:0.1.1 -f ./Dockerfile ../..
docker build --build-arg JAR_FILE=build/libs/miftp-0.1.1.jar --build-arg HTTP_PORT=8081 -t mibo/rpi-miftp -f ./Dockerfile ../..
