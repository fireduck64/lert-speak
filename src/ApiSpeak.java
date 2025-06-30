package duckutil.lertspeak;

import duckutil.Config;
import duckutil.ConfigFile;
import duckutil.lertspeak.cast.CastMgr;
import duckutil.lertspeak.voice.VoiceMedia;
import java.util.logging.Logger;

import duckutil.webserver.DuckWebServer;
import duckutil.webserver.WebContext;
import duckutil.webserver.WebHandler;
import java.net.URI;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class ApiSpeak implements WebHandler
{
  private static final Logger logger = Logger.getLogger("duckutil.lertspeak");

  public static void main(String args[]) throws Exception
  {
    if (args.length != 1)
    {
      System.out.println("Syntax: ApiSpeak config_file");

      System.exit(-1);
    }
    ConfigFile config = new ConfigFile(args[0], "");
    new ApiSpeak(config);
  }

  private CastMgr cast_mgr;
  private VoiceMedia voice_media;
  private String default_voice_id;

  public ApiSpeak(Config config)
    throws Exception
  {
    default_voice_id = config.get("media_voice");
    cast_mgr = new CastMgr(config);

    voice_media = new VoiceMedia(config);

    Thread.sleep(7500);

    cast_mgr.showPlayers();

    config.require("api_port");
    int api_port = config.getInt("api_port");

    new DuckWebServer(config.get("api_host"), api_port, this, 16);
    String host = config.get("api_host");
    if (host == null) host = "*";

    System.out.println("Listening on http://"+host+":" + api_port + "/");

  }
  public void say(String voice_id, String message)
    throws Exception
  {
    cast_mgr.playMedia(voice_media.getMediaURL(voice_id, message));

  }

  public void handle(WebContext t) throws Exception
  {
    URI uri = t.getURI();
    String path = uri.getPath();

    String method=t.getRequestMethod();
    System.out.println("Path: " + path);
    System.out.println("Method: " + method);
    if (!method.equals("POST")) return;
    if (!path.equals("/api/v1/say")) return;

    JSONParser parser = new JSONParser( JSONParser.MODE_PERMISSIVE );
    JSONObject req = (JSONObject) parser.parse(t.getRequestBody());

    String voice_id = default_voice_id;

    if (req.containsKey("voice_id"))
    {
      voice_id = (String) req.get("voice_id");
    }
    String words = (String) req.get("msg");

    say(voice_id, words);

    t.setHttpCode(200);
    t.out().println("Cool");

  }
}
