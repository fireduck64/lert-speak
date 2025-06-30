#!/bin/bash

docker container stop apispeak
docker container rm apispeak
docker pull 1209k/apispeak

docker run --name apispeak --restart always -d --network host \
  -e "TZ=America/Los_Angeles" \
  -e "LC_TIME=C.UTF-8" \
  -v $(pwd)/conf:/conf -v lertspeak-cache:/cache \
  1209k/apispeak

