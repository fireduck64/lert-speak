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
    "@maven//:org_jupnp_org_jupnp",
    "@maven//:javax_servlet_javax_servlet_api",
    "@maven//:org_eclipse_jetty_jetty_server",
  ],
)

java_binary(
  name = "LertSpeak",
  main_class = "duckutil.lertspeak.LertSpeak",
  runtime_deps = [
    ":lertspeaklib",
  ],
)

java_binary(
  name = "NoiseTest",
  main_class = "duckutil.lertspeak.upnp.NoiseTest",
  runtime_deps = [
    ":lertspeaklib",
  ],
)



