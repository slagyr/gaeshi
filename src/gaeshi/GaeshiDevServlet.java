package gaeshi;

import clojure.lang.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GaeshiDevServlet extends GaeshiServlet
{
  private static final Object lock = new Object();
  private static long lastRefreshTime;

  private static Thread refreshTread = new Thread(new Refreshener());
  private static final Object monitor = new Object();

  static
  {
    refreshTread.start();
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
      throw new ServletException(e);
    }
  }

  private static void reloadClojureSrc() throws Exception
  {
    synchronized(lock)
    {
      if(System.currentTimeMillis() > (lastRefreshTime + 1000))
      {
        synchronized(monitor)
        {
          monitor.notify();
          monitor.wait();
        }
        lastRefreshTime = System.currentTimeMillis();
      }
    }
  }

  private static class Refreshener implements Runnable
  {
    private Var refreshFn;

    private Refreshener()
    {
      refreshFn = RT.var("gaeshi.servlet", "refresh!");
    }

    public void run()
    {
      while(true)
      {
        try
        {
          synchronized(monitor)
          {
            monitor.wait();
          }
          refreshFn.invoke();
          synchronized(monitor)
          {
            monitor.notify();
          }
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    }
  }
}
