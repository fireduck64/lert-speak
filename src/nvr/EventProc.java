package duckutil.lertspeak.nvr;

import duckutil.Config;
import duckutil.LRUCache;
import duckutil.PeriodicThread;
import duckutil.lertspeak.LertSpeak;
import java.net.URL;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import java.util.logging.Logger;

public class EventProc extends PeriodicThread
{
  private static final Logger logger = Logger.getLogger("duckutil.lertspeak.nvr");

  Config conf;
  LinkedBlockingQueue<JSONObject> queue;
  TreeMap<String, String> name_map;
  LertSpeak speak;

  LRUCache<String, Long> message_times=new LRUCache<>(2000);

  TreeSet<String> allow_cameras=new TreeSet<>();
  TreeSet<String> disallow_cameras=new TreeSet<>();

  private long min_repeat_time_ms;

  public EventProc(Config conf, LinkedBlockingQueue<JSONObject> queue, LertSpeak speak)
    throws Exception
  {
    super(20);
    name_map = new TreeMap<>();
    this.speak = speak;

    this.conf = conf;
    this.queue = queue;

    conf.require("nvr_host");
    conf.require("nvr_api_key");

    min_repeat_time_ms = conf.getIntWithDefault("min_repeat_time", 60) * 1000L;

    if (conf.isSet("nvr_camera_allow"))
    {
      allow_cameras.addAll( conf.getList("nvr_camera_allow"));
    }
    else
    {
      allow_cameras.add("ALL");
    }
    if (conf.isSet("nvr_camera_disallow"))
    {
      disallow_cameras.addAll( conf.getList("nvr_camera_disallow"));
    }


    System.out.println(name_map);

  }

  public void runPass()
    throws Exception
  {
    JSONObject msg = queue.poll(20, TimeUnit.SECONDS);

    if (msg == null) return;

    JSONObject item = (JSONObject) msg.get("item");

    String camera_id = (String)item.get("device");

    String camera_name = getCameraName(camera_id);
    if (!shouldSpeak(camera_name))
    {
      logger.info(String.format("Camera (%s) disallowed by policy", camera_name)); 
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append(camera_name);

    String type = (String) item.get("type");

    if (type.equals(" motion"))
    {
      sb.append(" saw");
      sb.append(" motion");
    }
    else if (type.equals("smartDetectZone"))
    {

      sb.append(" saw");
      JSONArray stuff = (JSONArray) item.get("smartDetectTypes");
      for(Object o : stuff)
      {
        sb.append(" a " + o.toString());
      }
    }
    else if (type.equals("smartAudioDetect"))
    {
      sb.append(" heard");
      JSONArray stuff = (JSONArray) item.get("smartDetectTypes");
      for(Object o : stuff)
      {
        String n = o.toString();
        if (n.equals("alrmBark"))
        {
          n = "barking";
        }
        if (n.equals("alrmSpeak"))
        {
          n = "speaking";
        }
        sb.append(" " + n);
      }
      if (stuff.size() ==0)
      {
        sb.append(" noise");
      }

    }
    else
    {
      sb.append(" saw");
      sb.append(" ");
      sb.append(type);
    }
    System.out.println("MESSAGE: " + sb.toString());
    String msg_str = sb.toString();


    synchronized(message_times)
    {
      long last =0;
      if (message_times.containsKey(msg_str))
      {
        last = message_times.get(msg_str);
      }
      if (System.currentTimeMillis() > last + min_repeat_time_ms)
      {
        message_times.put(msg_str, System.currentTimeMillis());
        speak.say(sb.toString());
      }
    }

  }

  private boolean shouldSpeak(String camera_name)
  {
    if (disallow_cameras.contains(camera_name)) return false;

    if (allow_cameras.contains("ALL")) return true;
    if (allow_cameras.contains(camera_name)) return true;
    
    return false;

  }

  public String getCameraName(String id)
    throws Exception
  {
    if (name_map.containsKey(id)) return name_map.get(id);

    String url_str = "https://" + conf.get("nvr_host") + "/proxy/protect/integration/v1/cameras/" + id;

    URL u = new URL(url_str);

    HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();

    SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
    sslContextFactory.setTrustAll(true);
    sslContextFactory.setEndpointIdentificationAlgorithm(null);
    sslContextFactory.start();

    conn.setSSLSocketFactory( sslContextFactory.getSslContext().getSocketFactory());
    conn.setRequestMethod("GET");

    conn.setRequestProperty("X-API-KEY", conf.get("nvr_api_key"));
    conn.setRequestProperty("Accept","application/json");

    JSONParser parser = new JSONParser( JSONParser.MODE_PERMISSIVE );

    JSONObject camera_json = (JSONObject) parser.parse( conn.getInputStream() );

    conn.getInputStream().close();

    String name = (String) camera_json.get("name");

    name_map.put(id, name);

    return name;
  }


}
