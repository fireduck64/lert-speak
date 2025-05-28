package duckutil.lertspeak;

import duckutil.Config;
import duckutil.ConfigFile;
import duckutil.lertspeak.cast.CastMgr;
import duckutil.lertspeak.voice.VoiceMedia;
import java.util.Scanner;
import java.util.logging.Logger;

public class InteractiveSpeak
{
  private static final Logger logger = Logger.getLogger("duckutil.lertspeak");

  public static void main(String args[]) throws Exception
  {
    if (args.length != 1)
    {
      System.out.println("Syntax: InteractiveSpeak config_file");

      System.exit(-1);
    }
    ConfigFile config = new ConfigFile(args[0], "");
    new InteractiveSpeak(config);
  }

  private CastMgr cast_mgr;
  private VoiceMedia voice_media;

  public InteractiveSpeak(Config config)
    throws Exception
  {
    cast_mgr = new CastMgr(config);

    voice_media = new VoiceMedia(config);

    Thread.sleep(10000);

    cast_mgr.showPlayers();


    Scanner scan = new Scanner(System.in);

    System.out.println("Provide lines to speak, hit enter.");

    System.out.print("> ");


    while(scan.hasNextLine())
    {
      String line = scan.nextLine();
      say(line);

      System.out.print("> ");

    }

    System.exit(0);

  }
  public void say(String message)
    throws Exception
  {
    cast_mgr.playMedia(voice_media.getMediaURL(message));

  }


}
