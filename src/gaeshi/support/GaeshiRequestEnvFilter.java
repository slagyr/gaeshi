package gaeshi.support;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.users.dev.LoginCookieUtils;
import com.google.appengine.tools.development.LocalEnvironment;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.utils.config.AppEngineWebXml;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class GaeshiRequestEnvFilter implements Filter
{
  private AppEngineWebXml appEngineWebXml;

  public GaeshiRequestEnvFilter(AppEngineWebXml appEngineWebXml)
  {
    this.appEngineWebXml = appEngineWebXml;
  }

  public void init(FilterConfig filterConfig) throws ServletException
  {
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
  {
    LocalEnvironment env = new LocalHttpRequestEnvironment(appEngineWebXml, (HttpServletRequest)servletRequest);
    ApiProxy.setEnvironmentForCurrentThread(env);
    try
    {
      filterChain.doFilter(servletRequest, servletResponse);
    }
    finally
    {
      ApiProxy.clearEnvironmentForCurrentThread();
    }
  }

  public void destroy()
  {
  }

  private static class LocalHttpRequestEnvironment extends LocalEnvironment
  {
    static final String DEFAULT_NAMESPACE_HEADER = "X-AppEngine-Default-Namespace";
    static final String CURRENT_NAMESPACE_HEADER = "X-AppEngine-Current-Namespace";
    private static final String CURRENT_NAMESPACE_KEY = NamespaceManager.class.getName() + ".currentNamespace";
    private static final String APPS_NAMESPACE_KEY = NamespaceManager.class.getName() + ".appsNamespace";
    private final com.google.appengine.api.users.dev.LoginCookieUtils.CookieData loginCookieData;

    public LocalHttpRequestEnvironment(AppEngineWebXml appEngineWebXml, HttpServletRequest request)
    {
      super(appEngineWebXml);
      loginCookieData = LoginCookieUtils.getCookieData(request);
      String requestNamespace = request.getHeader("X-AppEngine-Default-Namespace");
      if(requestNamespace != null)
        attributes.put(APPS_NAMESPACE_KEY, requestNamespace);
      String currentNamespace = request.getHeader("X-AppEngine-Current-Namespace");
      if(currentNamespace != null)
        attributes.put(CURRENT_NAMESPACE_KEY, currentNamespace);
      if(loginCookieData != null)
      {
        attributes.put("com.google.appengine.api.users.UserService.user_id_key", loginCookieData.getUserId());
        attributes.put("com.google.appengine.api.users.UserService.user_organization", "");
      }
    }

    public boolean isLoggedIn()
    {
      return loginCookieData != null;
    }

    public String getEmail()
    {
      if(loginCookieData == null)
        return null;
      else
        return loginCookieData.getEmail();
    }

    public boolean isAdmin()
    {
      return loginCookieData != null && loginCookieData.isAdmin();
    }

  }

}
