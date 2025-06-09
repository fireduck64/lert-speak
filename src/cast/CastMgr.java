package duckutil.lertspeak.cast;

import duckutil.Config;
import duckutil.PeriodicThread;
import java.net.InetAddress;
import java.net.URL;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import su.litvak.chromecast.api.v2.Application;
import su.litvak.chromecast.api.v2.ChromeCast;
import su.litvak.chromecast.api.v2.ChromeCasts;
import su.litvak.chromecast.api.v2.Status;

public class CastMgr
{
  private static final Logger logger = Logger.getLogger("duckutil.lertspeak.cast");

  Config conf;
  public CastMgr(Config conf)
    throws Exception
  {
    this.conf = conf;

    conf.require("cast_speakers");

    selected_players = new TreeSet<>();
    selected_players.addAll( conf.getList("cast_speakers"));
    new CastDiscoThread().start();

  }
  private String APP_ID="CC1AD845";

  private TreeSet<String> selected_players;

  private TreeMap<String, ChromeCast> known_casts=new TreeMap<>();

  public class CastDiscoThread extends PeriodicThread
  {
    public CastDiscoThread()
    {
      super(600000L);
    }

    boolean first_pass_done=false;

    public void runPass() throws Exception
    {

      if (conf.isSet("cast_local_ip"))
      {
        ChromeCasts.startDiscovery(InetAddress.getByName(conf.get("cast_local_ip")));
      }
      ChromeCasts.startDiscovery();

      TreeMap<String, ChromeCast> new_casts = new TreeMap<>();

      for(int i=0; i<45; i++)
      {
        Thread.sleep(1000);

        for(ChromeCast cc : ChromeCasts.get())
        {
          String title = cc.getTitle();
          new_casts.put(title, cc);
        }

        if (!first_pass_done)
        {
          TreeMap<String, ChromeCast> copy=new TreeMap<>();
          copy.putAll(new_casts);
          known_casts = copy;
        }
      }

      known_casts = new_casts;

      ChromeCasts.stopDiscovery();
      first_pass_done=true;

    }


  }

  public class SpeakThread extends Thread
  {

    URL url;
    ChromeCast cc;

    public SpeakThread(ChromeCast cc, URL url)
    {
      this.cc = cc;
      this.url = url;

    }
    public void run()
    {
      try
      {
        String title = cc.getTitle();
        if (includePlayer(title))
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

  public synchronized void showPlayers()
  {
    int seen=0;
    int selected=0;
    for(ChromeCast cc : known_casts.values())
    {
      String title = cc.getTitle();
      logger.info("player seen: " + cc.getTitle());
      seen++;
      if (includePlayer(title))
      {
        logger.info("player selected: "+ cc.getTitle());
        selected++;
      }
    }
    logger.info(String.format("Players seen: %d   Players selected: %d", seen, selected));

  }
  protected synchronized boolean includePlayer(String title)
  {
    if (selected_players.contains("ALL")) return true;
    if (selected_players.contains(title)) return true;

    return false;

  }

  public synchronized void playMedia(URL url)
    throws Exception
  {
    for(ChromeCast cc : known_casts.values())
    {
      new SpeakThread(cc, url).start();
    }

  }


}
