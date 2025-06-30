FROM gcr.io/bazel-public/bazel:7.4.0 AS build

RUN mkdir lertspeak
WORKDIR /lertspeak

COPY BUILD .
#COPY MODULE.bazel .
COPY WORKSPACE .
COPY src src

RUN bazel build :all :ApiSpeak_deploy.jar :LertSpeak_deploy.jar

FROM debian AS run

RUN apt-get update
RUN apt-get -y install openjdk-17-jre-headless locales festival

RUN update-locale LC_TIME=C.UTF-8

COPY --from=build /lertspeak/bazel-bin/LertSpeak_deploy.jar /LertSpeak_deploy.jar

RUN mkdir -p /conf
RUN touch /conf/lertspeak.conf

ENTRYPOINT ["java", "-jar", "/LertSpeak_deploy.jar", "/conf/lertspeak.conf" ]

