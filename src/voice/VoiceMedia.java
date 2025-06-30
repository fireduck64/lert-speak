package duckutil.lertspeak.voice;

import duckutil.AtomicFileOutputStream;
import duckutil.Config;
import duckutil.webserver.DuckWebServer;
import duckutil.webserver.WebContext;
import duckutil.webserver.WebHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.TreeSet;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;

public class VoiceMedia implements WebHandler
{
  private static final Logger logger = Logger.getLogger("duckutil.lertspeak.voice");

  File cache_dir;
  String default_voice_id;
  String media_host;
  int media_port;
  String api_key;

  String voice_mode;

  // We are only going to return files that are expected to be asked for
  TreeSet<String> expected_files;

  public VoiceMedia(Config conf)
    throws Exception
  {
    cache_dir = new File(conf.require("media_cache_path"));

    voice_mode = conf.getWithDefault("voice_mode","elevenlabs");

    if (voice_mode.equals("elevenlabs"))
    {
      default_voice_id = conf.require("media_voice");
      api_key = conf.require("elevenlabs_api_key");
    }
    else if (voice_mode.equals("festival"))
    {

    }
    else
    {
      throw new Exception("Unknown voice mode: " + voice_mode);
    }

    media_host = conf.require("media_host");
    conf.require("media_port");
    media_port = conf.getInt("media_port");

    cache_dir.mkdirs();

    expected_files = new TreeSet<>();

    new DuckWebServer(null, media_port, this, 16);

  }
  public URL getMediaURL(String statement)
    throws Exception
  {
    return getMediaURL(default_voice_id, statement);
  }

  public URL getMediaURL(String voice_id, String statement)
    throws Exception
  {
    if (voice_mode.equals("festival"))
    {
      return getMediaURLFestival(voice_mode, statement);
    }
    else
    {
      return getMediaURLElevenLabs(voice_id, statement);
    }

  }

  public URL getMediaURLFestival(String voice_id, String statement)
    throws Exception
  {
    String cache_file_name=voice_id + "_" + HUtil.getHash(statement) + ".wav";
    File cache_file = new File(cache_dir, cache_file_name);
    URL url = new URL("http://" + media_host +":"+media_port +"/" + cache_file.getName());
    synchronized(expected_files)
    {
      expected_files.add(cache_file_name);
    }
    if (cache_file.exists())
    {
      return url;
    }

    logger.info("Sound file not in cache, generating with festival...");

    byte[] buff = new byte[8192];

    Process proc = Runtime.getRuntime().exec("text2wave");
    PrintStream fest_cmd = new PrintStream(proc.getOutputStream());

    fest_cmd.println(statement);
    fest_cmd.flush();
    fest_cmd.close();

    AtomicFileOutputStream file_out = new AtomicFileOutputStream(cache_file);
    while(true)
    {
      int r = proc.getInputStream().read(buff);
      if (r == -1) break;
      file_out.write(buff, 0, r);
    }
    int rv= proc.waitFor();
    if (rv == 0)
    {
      file_out.flush();
      file_out.close();
    }
    else
    {
      file_out.abort();
      return null;
    }

    return url;

  }

  public URL getMediaURLElevenLabs(String voice_id, String statement)
    throws Exception
  {

    String cache_file_name=voice_id + "_" + HUtil.getHash(statement) + ".mp3";
    File cache_file = new File(cache_dir, cache_file_name);
    URL url = new URL("http://" + media_host +":"+media_port +"/" + cache_file.getName());
    synchronized(expected_files)
    {
      expected_files.add(cache_file_name);
    }
    if (cache_file.exists())
    {
      return url;
    }

    logger.info("Sound file not in cache, generating with elevenlabs...");
    long t1 = System.currentTimeMillis();

    URL q = new URL("https://api.elevenlabs.io/v1/text-to-speech/" + voice_id);
    HttpURLConnection conn = (HttpURLConnection) q.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestProperty("Xi-Api-Key", api_key);
    conn.setRequestProperty("Content-Type", "application/json");

    OutputStream wr = conn.getOutputStream();
    JSONObject post_req = new JSONObject();

    post_req.put("text", statement);
    post_req.put("model_id", "eleven_multilingual_v2");

    wr.write(post_req.toString().getBytes("UTF-8"));
    wr.flush();
    wr.close();

    byte[] buff = new byte[8192];

    AtomicFileOutputStream file_out = new AtomicFileOutputStream(cache_file);
    while(true)
    {
      int r = conn.getInputStream().read(buff);
      if (r == -1) break;
      file_out.write(buff, 0, r);

    }
    file_out.flush();
    file_out.close();
    conn.getInputStream().close();

    long t2 = System.currentTimeMillis();
    double sec = (t2 - t1) / 1000.0;
    DecimalFormat df = new DecimalFormat("0.000");
    logger.info(String.format("Voice generation took %s seconds", df.format(sec)));

    return url;
  }

  public void handle(WebContext t) throws Exception
  {
    URI uri = t.getURI();

    String path = uri.getPath();
    path = path.substring(1);
    System.out.println(path);

    synchronized(expected_files)
    {
      if (!expected_files.contains(path))
      {
        t.setException(new Exception("unexpected request"));
        return;
      }
    }

    File sound_file = new File(cache_dir, path);
    t.setHttpCode(200);
    if (sound_file.getName().endsWith(".mp3"))
    {
      t.setContentType("audio/mpeg");
    }
    if (sound_file.getName().endsWith(".wav"))
    {
      t.setContentType("audio/x-wav");
    }
    t.setOutputSize(sound_file.length());

    t.writeHeaders();
    FileInputStream f_in = new FileInputStream(sound_file);
    byte b[]=new byte[8192];
    while(true)
    {
      int r = f_in.read(b);
      if (r == -1) break;
      t.getOutStream().write(b,0,r);
    }
    f_in.close();
    t.getOutStream().close();

  }
}
