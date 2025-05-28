#!/bin/bash

set -eu

docker build . -t 1209k/lertspeak
docker push 1209k/lertspeak

docker build . -f api.Dockerfile -t 1209k/apispeak
docker push 1209k/apispeak

