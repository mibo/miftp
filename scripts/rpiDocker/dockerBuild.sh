#!/usr/bin/env bash

HTTP_PORT=8081
REPO_IMAGE=mibo/rpi-miftp

if [[ -z $1 ]]; then
  echo "No version parameter given. Stop build."
  exit 1
elif [[ ${1} =~ ^([0-9]\.){2}[0-9](-SNAPSHOT)?$ ]]; then
  VERSION=$1
else
  echo "Invalid version $1 parameter (format must be e.g. '1.2.3' (optional '-SNAPSHOT'))"
#  echo "Found version $1"
fi
echo "Start with $VERSION"

if [[ ${VERSION} =~ .*-SNAPSHOT ]]; then
  # SNAPSHOT
  echo "SNAPSHOT version"
  CMD="docker build --build-arg JAR_FILE=build/libs/miftp-${VERSION}.jar --build-arg HTTP_PORT=${HTTP_PORT} -t ${REPO_IMAGE}:sn -f ./Dockerfile ../.."
else
  echo "RELEASE version"
  #RELEASES
  CMD="docker build --build-arg JAR_FILE=build/libs/miftp-${VERSION}.jar --build-arg HTTP_PORT=${HTTP_PORT} -t ${REPO_IMAGE} -t ${REPO_IMAGE}:${VERSION} -f ./Dockerfile ../.."
fi

echo "Start build..."
echo "${CMD}"
echo ""
${CMD}

