package duckutil.lertspeak.nvr;

import duckutil.Config;
import duckutil.PeriodicThread;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.AbstractQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

// Your WebSocket endpoint class
@org.eclipse.jetty.websocket.api.annotations.WebSocket
public class JsonEventSocket {

  private static final Logger logger = Logger.getLogger("duckutil.lertspeak.nvr");
  @org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
  public void onConnect(Session session) {
    logger.info("Connected to server: " + session.getRemoteAddress());
    connected=true;
    new PingThread(session).start();
  }

  @org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
  public void onMessage(String message) {
    logger.info("Received JSON event: " + message);

    try
    {
      JSONParser parser = new JSONParser( JSONParser.MODE_PERMISSIVE );
      JSONObject msg = (JSONObject) parser.parse(message);

      queue.add(msg);
    }
    catch(net.minidev.json.parser.ParseException e)
    {
      logger.log(Level.WARNING, "JSON parse error", e);

    }

  }

  @org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    logger.info("WebSocket closed: " + statusCode + " - " + reason);
    connected=false;
  }

  @org.eclipse.jetty.websocket.api.annotations.OnWebSocketError
  public void onError(Throwable cause) {
    logger.log(Level.WARNING, "WebSocket error " + cause);

    connected=false;
  }

  boolean connected=false;
  Config conf;
  AbstractQueue<JSONObject> queue;

  public JsonEventSocket(Config conf, AbstractQueue<JSONObject> queue)
    throws Exception
  {
    this.conf = conf;
    this.queue = queue;
    new ConnectThread(this).start();

  }

  public class ConnectThread extends PeriodicThread
  {
    private JsonEventSocket sock;
    private WebSocketClient client;
    public ConnectThread(JsonEventSocket sock)
    {
      super(15000);
      this.sock = sock;
    }

    public void runPass()
      throws Exception
    {
      try
      {
        if (!connected)
        {
          if (client != null)
          {
            // Must be connection failed
            client.stop();
          }

          String nvr_host = conf.require("nvr_host");
          String nvr_api_key = conf.require("nvr_api_key");

          String destUri = "wss://"+nvr_host +"/proxy/protect/integration/v1/subscribe/events";

          // For untrusted certificates
          SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
          sslContextFactory.setTrustAll(true);
          sslContextFactory.setEndpointIdentificationAlgorithm(null);

          client = new WebSocketClient(sslContextFactory);

          client.start();
          URI echoUri = new URI(destUri);
          ClientUpgradeRequest request = new ClientUpgradeRequest();
          request.setHeader("X-API-KEY", nvr_api_key);
          client.connect(sock, echoUri, request).get(5, TimeUnit.SECONDS); // Connect and wait
          logger.info("NVR Connection attempt initiated.");
        }
      }
      catch(Exception e)
      {
        logger.log(Level.WARNING, "NVR error - " + e);

      }

    }



  }

  public class PingThread extends PeriodicThread
  {
    private Session session;
    public PingThread(Session session)
    {
      super(15000L);
      this.session = session;
    }

    @Override
    public void runPass()
      throws Exception
    {
      if (!connected)
      {
        halt();
        return;
      }
      ByteBuffer payload = ByteBuffer.wrap("KeepAlivePing".getBytes());
      session.getRemote().sendPing(payload);

    }

  }
}
