
package duckutil.lertspeak.cast;

import su.litvak.chromecast.api.v2.ChromeCasts;
import su.litvak.chromecast.api.v2.ChromeCast;

import su.litvak.chromecast.api.v2.Application;
import su.litvak.chromecast.api.v2.Status;

import duckutil.Config;
import java.util.TreeSet;
import java.net.URL;
import java.net.InetAddress;
import java.util.logging.Logger;

public class CastMgr
{
  private static final Logger logger = Logger.getLogger("duckutil.lertspeak.cast");

  public CastMgr(Config conf)
    throws Exception
  {
    if (conf.isSet("cast_local_ip"))
    {
      ChromeCasts.startDiscovery(InetAddress.getByName(conf.get("cast_local_ip")));
    }
    ChromeCasts.startDiscovery();

    conf.require("cast_speakers");

    selected_players = new TreeSet<>();

    selected_players.addAll( conf.getList("cast_speakers"));

  }
  private String APP_ID="CC1AD845";

  private TreeSet<String> selected_players;

  public synchronized void showPlayers()
  {
    int seen=0;
    int selected=0;
    for(ChromeCast cc : ChromeCasts.get())
    {
      String title = cc.getTitle();
      logger.info("player seen: " + cc.getTitle());
      seen++;
      if (selected_players.contains(title))
      {
        logger.info("player selected: "+ cc.getTitle());
        selected++;
      }
    }
    logger.info(String.format("Players seen: %d   Players selected: %d", seen, selected));

  }

  public synchronized void playMedia(URL url)
    throws Exception
  {
    for(ChromeCast cc : ChromeCasts.get())
    {
      try
      {
        String title = cc.getTitle();
        if (selected_players.contains(title))
        {
          Status status = cc.getStatus();
          if (cc.isAppAvailable(APP_ID) && !status.isAppRunning(APP_ID)) {
            Application app = cc.launchApp(APP_ID);
          }
          cc.load(url.toString());
   
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }

    }

  }
  

}
