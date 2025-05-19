
package duckutil.lertspeak;

import duckutil.lertspeak.cast.CastMgr;
import duckutil.Config;
import duckutil.ConfigFile;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import net.minidev.json.JSONObject;
import duckutil.lertspeak.nvr.JsonEventSocket;
import duckutil.lertspeak.voice.VoiceMedia;
import duckutil.lertspeak.nvr.EventProc;
import java.util.logging.Logger;


public class LertSpeak
{
  private static final Logger logger = Logger.getLogger("duckutil.lertspeak");

  public static void main(String args[]) throws Exception
  {
    if (args.length != 1)
    {
      System.out.println("Syntax: LertSpeak config_file");

      System.exit(-1);
    }
    ConfigFile config = new ConfigFile(args[0], "");
    new LertSpeak(config);
  }

  private CastMgr cast_mgr;
  private VoiceMedia voice_media;

  public LertSpeak(Config config)
    throws Exception
  {
    cast_mgr = new CastMgr(config);

    voice_media = new VoiceMedia(config);

    LinkedBlockingQueue<JSONObject> queue = new LinkedBlockingQueue<>();

    new JsonEventSocket(config, queue);

    new EventProc(config, queue, this).start();

    Thread.sleep(5000);

    cast_mgr.showPlayers();

    cast_mgr.playMedia(voice_media.getMediaURL("Crabs rarely go to college."));

    while(true)
    {
      Thread.sleep(10000);
      cast_mgr.showPlayers();

    }

    //cast_mgr.playMedia(new URL("https://bulk.1209k.com/freesound/Anttis%20Instrumentals%208%209%202017/Songs/anttisinstrumentals%2B1987.mp3"));

  }
  public void say(String message)
    throws Exception
  {
    cast_mgr.playMedia(voice_media.getMediaURL(message));

  }


}
