package gaeshi.tsukuri;

import com.google.appengine.tools.development.ApiProxyLocal;
import com.google.appengine.tools.development.ApiProxyLocalFactory;
import com.google.appengine.tools.development.LocalServerEnvironment;
import com.google.apphosting.api.ApiProxy;
import java.io.File;

public class GaeshiDevServerEnvironment implements LocalServerEnvironment
{
  public String address = "127.0.0.1";
  public int port = 8080;
  public String env = "development";
  public String dir = ".";
  public String hostName = "localhost";

  public void install()
  {
    final ApiProxyLocalFactory apiProxyLocalFactory = new ApiProxyLocalFactory();
    final ApiProxyLocal apiProxyLocal = apiProxyLocalFactory.create(this);
    ApiProxy.setDelegate(apiProxyLocal);
  }

  public String getConfig(String name, String defaultValue)
  {
    String value = System.getProperty(name);
    return value == null ? defaultValue : value;
  }

  public File getAppDir()
  {
    return new File(dir);
  }

  public String getAddress()
  {
    return address;
  }

  public int getPort()
  {
    return port;
  }

  public String getHostName()
  {
    return hostName;
  }

  public void waitForServerToStart() throws InterruptedException
  {
  }

  public boolean enforceApiDeadlines()
  {
    return false;
  }

  public boolean simulateProductionLatencies()
  {
    return false;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder("Gaeshi Dev Server [");
    builder.append("ENV: ").append(env);
    builder.append(", PORT: ").append(port);
    builder.append(", ADDRESS: ").append(address);
    builder.append(", DIR: ").append(dir);
    builder.append("]");
    return builder.toString();
  }
}
