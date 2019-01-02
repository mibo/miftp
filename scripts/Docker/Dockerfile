FROM openjdk:8-jdk-alpine
MAINTAINER mibo@apache.org

VOLUME /tmp
ARG FTP_PORT=50021
ARG HTTP_PORT=8080
ARG JAR_FILE=build/libs/miftp-latest.jar
COPY ${JAR_FILE} app.jar
EXPOSE ${HTTP_PORT}
EXPOSE ${FTP_PORT}

ENV SERVER_PORT=${HTTP_PORT}
ENV MIFTP_FTP_PORT=${FTP_PORT}

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]