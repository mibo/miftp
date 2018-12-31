FROM openjdk:8-jdk-alpine
MAINTAINER mibo@apache.org

VOLUME /tmp
ARG JAR_FILE=build/libs/miftp-latest.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
EXPOSE 50021

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]