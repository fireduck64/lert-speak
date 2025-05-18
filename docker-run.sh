#!/bin/bash

docker container stop lertspeak
docker container rm lertspeak

docker run --name lertspeak --restart always -d --network host \
  -v $(pwd)/conf:/conf -v lertspeak-cache:/cache \
  1209k/lertspeak


