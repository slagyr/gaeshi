package gaeshi.tsukuri;

import com.google.apphosting.api.ApiProxy;

import java.util.HashMap;
import java.util.Map;

public class GaeshiApiProxyEnvironment implements ApiProxy.Environment
{
  public String appId = "default_app_id";
  public String versionId = "default_version_id";
  public String email = "default@email.com";
  public boolean isLoggedIn = false;
  public boolean isAdmin = true;
  public String authDomain = "default_auth_domain";
  public String requestNamespace = "default_request_namespace";
  public Map<String, Object> attributes = new HashMap<String, Object>();

  public GaeshiApiProxyEnvironment(String appId)
  {
    this.appId = appId;
  }

  public void install()
  {
    ApiProxy.setEnvironmentForCurrentThread(this);
  }

  public String getAppId()
  {
    return appId;
  }

  public String getVersionId()
  {
    return versionId;
  }

  public String getEmail()
  {
    return email;
  }

  public boolean isLoggedIn()
  {
    return isLoggedIn;
  }

  public boolean isAdmin()
  {
    return isAdmin;
  }

  public String getAuthDomain()
  {
    return authDomain;
  }

  public String getRequestNamespace()
  {
    return requestNamespace;
  }

  public Map<String, Object> getAttributes()
  {
    return attributes;
  }

  public long getRemainingMillis()
  {
    return 1000;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder("Gaeshi API Proxy Environment [");
    builder.append("App: ").append(appId);
    builder.append(", Version: ").append(versionId);
    builder.append(", Email: ").append(email);
    builder.append("]");
    return builder.toString();
  }
}
