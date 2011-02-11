package gaeshi;

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
      throw new ServletException(e);
    }
  }

  private static void reloadClojureSrc() throws Exception
  {
    if(refreshFn == null)
      refreshFn = RT.var("gaeshi.servlet", "refresh!");
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
