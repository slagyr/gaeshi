package gaeshi.support;

import clojure.lang.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GaeshiDevServlet extends GaeshiServlet
{
  private static Var refreshFn;
  private static final Object lock = new Object();
  private static long lastRefreshTime;

  public GaeshiDevServlet() throws Exception
  {
    super();
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    try
    {
      reloadClojureSrc();
      loadServiceMethod();
      serviceMethod.invoke(this, req, resp);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      e.printStackTrace(resp.getWriter());
      resp.getWriter().close();
      resp.setContentType("text/plain");
    }
  }

  private static void reloadClojureSrc() throws Exception
  {
    if(refreshFn == null)
      refreshFn = loadVar("gaeshi.support.servlet", "refresh!");
    synchronized(lock)
    {
      if(System.currentTimeMillis() > (lastRefreshTime + 1000))
      {
        refreshFn.invoke();
        lastRefreshTime = System.currentTimeMillis();
      }
    }
  }
}
