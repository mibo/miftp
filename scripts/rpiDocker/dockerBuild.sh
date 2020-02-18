#!/usr/bin/env bash

HTTP_PORT=8081
REPO_IMAGE=mibo/rpi-miftp
PUSH=false
SNAPSHOT=false

if [[ -z $1 ]]; then
  echo "No version parameter given. Stop build."
  exit 1
elif [[ ${1} =~ ^([0-9]+\.){2}[0-9](-SNAPSHOT)?$ ]]; then
  VERSION=$1
  if [[ ${VERSION} =~ .*-SNAPSHOT ]]; then
    SNAPSHOT=true
    echo "Identified a SNAPSHOT for version $VERSION"
  fi
else
  echo "Invalid version $1 parameter (format must be e.g. '1.2.3' (optional '-SNAPSHOT'))"
#  echo "Found version $1"
fi

if [[ -z $2 ]]; then
  echo "No second parameter given. Use default push=${PUSH}"
elif [[ ${2} == "push" ]]; then
  echo "Enable push after build."
  PUSH=true
fi

echo "Start docker build for version $VERSION"
echo

if [[ ${SNAPSHOT} == true ]]; then
  # SNAPSHOT
  echo "Prepare for SNAPSHOT version"
  CMD="docker build --build-arg JAR_FILE=build/libs/miftp-${VERSION}.jar --build-arg HTTP_PORT=${HTTP_PORT} -t ${REPO_IMAGE}:sn -f ./Dockerfile ../.."
else
  #RELEASES
  echo "Prepare for RELEASE version"
  CMD="docker build --build-arg JAR_FILE=build/libs/miftp-${VERSION}.jar --build-arg HTTP_PORT=${HTTP_PORT} -t ${REPO_IMAGE} -t ${REPO_IMAGE}:${VERSION} -f ./Dockerfile ../.."
fi

echo "Start build..."
echo "${CMD}"
echo ""
${CMD}

if [[ ${PUSH} == true ]]; then
  echo "Push..."
  if [[ ${SNAPSHOT} == true ]]; then
    docker push $REPO_IMAGE:sn
  else
    docker push $REPO_IMAGE:latest
    docker push $REPO_IMAGE:$VERSION
  fi
fi
