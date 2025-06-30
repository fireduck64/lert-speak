#!/bin/bash

docker container stop lertspeak
docker container rm lertspeak
docker pull 1209k/lertspeak

docker run --name lertspeak --restart always -d --network host \
  -e "TZ=America/Los_Angeles" \
  -e "LC_TIME=C.UTF-8" \
  -v $(pwd)/conf:/conf -v lertspeak-cache:/cache \
  1209k/lertspeak


