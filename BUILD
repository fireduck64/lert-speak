package(default_visibility = ["//visibility:public"])

java_library(
  name = "lertspeaklib",
  srcs = glob(["src/**/*.java", "src/*.java"]),
  deps = [
    "@duckutil//:duckutil_lib",
    "@duckutil//:webserver_lib",
    "@maven//:de_sfuhrm_chromecast_java_api_v2",
    "@maven//:commons_codec_commons_codec",
    "@maven//:net_minidev_json_smart",
    "@maven//:org_eclipse_jetty_websocket_websocket_client",
    "@maven//:org_eclipse_jetty_jetty_util",
    "@maven//:org_eclipse_jetty_websocket_websocket_api",
  ],
)

java_binary(
  name = "LertSpeak",
  main_class = "duckutil.lertspeak.LertSpeak",
  runtime_deps = [
    ":lertspeaklib",
  ],
)


