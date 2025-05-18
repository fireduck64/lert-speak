FROM ubuntu

RUN apt-get update
RUN apt-get install -y bazel-bootstrap
RUN apt-get install -y git

RUN mkdir lertspeak
WORKDIR /lertspeak

COPY BUILD .
COPY MODULE.bazel .
COPY WORKSPACE .
COPY src src

RUN bazel build :all
