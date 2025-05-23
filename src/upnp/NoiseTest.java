package duckutil.lertspeak.sonos;

import duckutil.ConfigFile;
import duckutil.Config;

import org.jupnp.UpnpService;
import org.jupnp.UpnpServiceImpl;
import org.jupnp.model.message.header.STAllHeader;
import org.jupnp.registry.DefaultRegistryListener;
import org.jupnp.DefaultUpnpServiceConfiguration;

public class NoiseTest
{
  public static void main(String args[])
    throws Exception
  {
    ConfigFile config = new ConfigFile(args[0], "");

    new NoiseTest(config);
  }

  public NoiseTest(Config conf)
    throws Exception
  {
    UpnpService upnpService = new UpnpServiceImpl(new DefaultUpnpServiceConfiguration());

    upnpService.startup();
    upnpService.getControlPoint().search(new STAllHeader());
    upnpService.getRegistry().addListener(new MagicListener());
    Thread.sleep(30000);
    upnpService.shutdown();
  }

  public class MagicListener extends DefaultRegistryListener
  {

  }
}
