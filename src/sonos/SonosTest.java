package duckutil.lertspeak.sonos;

import duckutil.ConfigFile;
import duckutil.Config;
import engineer.nightowl.sonos.api.SonosApiClient;
import engineer.nightowl.sonos.api.SonosApiConfiguration;

public class SonosTest
{
  public static void main(String args[])
    throws Exception
  {
    ConfigFile config = new ConfigFile(args[0], "");

    new SonosTest(config);
  }

  public SonosTest(Config conf)
  {
    conf.require("sonos_api_key");
    conf.require("sonos_key_name");
    conf.require("sonos_secret");
		final SonosApiConfiguration configuration = new SonosApiConfiguration();
		configuration.setApiKey(conf.get("sonos_api_key"));
		configuration.setApiSecret(conf.get("sonos_secret"));
		configuration.setApplicationId(conf.get("sonos_api_key"));

		final SonosApiClient client = new SonosApiClient(configuration);


  }
}
