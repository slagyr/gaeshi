package gaeshi;

import clojure.lang.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GaeshiDevServlet extends GaeshiServlet
{
  private Var refreshFn;

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

  private void reloadClojureSrc() throws Exception
  {
    if(refreshFn == null)
      refreshFn = RT.var("gaeshi.servlet", "refresh!");
    refreshFn.invoke();
  }
}
