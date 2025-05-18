package duckutil.lertspeak.voice;

import java.security.MessageDigest;

import java.util.*;
import org.apache.commons.codec.binary.Hex;

public class HUtil
{
  public static String getHash(String input)
  {
    try
    {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] b = md.digest(input.getBytes());
      return getHexString(b);
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }


  public static String getHexString(byte[] data)
  {
    Hex h = new Hex();
    return new String(h.encodeHex(data));
  }

}
