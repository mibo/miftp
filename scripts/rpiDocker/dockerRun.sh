#!/usr/bin/env bash
IMAGE=mibo/miftp
if [[ -n $1 ]]; then
  IMAGE=$1
fi
echo "Start with $IMAGE"

docker run --rm \
  -p 8081:8080 -p 50021:50021 \
  -p 40100-40200:40100-40200 \
  -v "$(pwd)"/../keystore.jks:/config/keystore.jks \
  -e MIFTP_KEYSTORE_NAME=config/keystore.jks \
  -e MIFTP_KEYSTORE_PASSWORD=password \
  -e SERVER_PORT=8080 \
  -e MIFTP_USER=miftp \
  -e MIFTP_PASSWORD="{bcrypt}\$2a\$10\$5SyjnpMano4Z3LGbWQC9W.ySSsheBZI.7uufzpJ4uKokBGfd.uHau" \
  -e MIFTP_FTP_PORT=50021 \
  -e MIFTP_FTP_USER=ftp  \
  -e MIFTP_FTP_PASSWORD=ftp \
  -e MIFTP_FTP_MAXFILES=30 \
  -e MIFTP_FTP_PASVADDRESS=172.17.0.2 \
  -e MIFTP_FTP_PASVEXTADDRESS=192.168.81.110 \
  -e MIFTP_FTP_PASVPORTS=40100-40200 \
  $IMAGE
