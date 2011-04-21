package gaeshi.support;

import clojure.lang.*;
import gaeshi.util.Clj;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GaeshiServlet extends HttpServlet
{
  protected IFn serviceMethod;
  private static Var makeServiceMethodFn;

  public GaeshiServlet()
  {
    try
    {
      loadServiceMethod();
    }
    catch(Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    try
    {
      serviceMethod.invoke(this, req, resp);
    }
    catch(Exception e)
    {
      throw new ServletException(e);
    }
  }

  protected static Var getMakeServiceMethodFn() throws Exception
  {
    if(makeServiceMethodFn == null)
    {
      RT.loadResourceScript("gaeshi/support/servlet.clj");
      makeServiceMethodFn = RT.var("gaeshi.support.servlet", "make-service-method");
    }
    return makeServiceMethodFn;
  }

  protected void loadServiceMethod() throws Exception
  {
    final String coreNamespace = getCoreNamespace();
    loadCoreNamespace(coreNamespace);
    Var handler = loadHandler(coreNamespace);

    serviceMethod = (IFn) getMakeServiceMethodFn().invoke(handler);
  }

  private Var loadHandler(String coreNamespace)
  {
    Var handler = null;
    try
    {
      handler = RT.var(coreNamespace, "gaeshi-handler");
      if(!(handler.deref() instanceof IFn))
        throw new Exception("Not an IFn");
    }
    catch(Exception e)
    {
      throw new RuntimeException(coreNamespace + "/gaeshi-handler must define a Ring handler", e);
    }
    return handler;
  }

  private void loadCoreNamespace(String coreNamespace)
  {
    try
    {
      final Symbol nsSymbol = Symbol.intern(null, coreNamespace);
      if(Namespace.find(nsSymbol) != null)
        return;
      RT.load(coreNamespace, false);

      if(Namespace.find(nsSymbol) != null)
        return;
      final String coreFilename = Clj.nsToFilename(coreNamespace);
      RT.loadResourceScript(coreFilename);

      if(Namespace.find(nsSymbol) != null)
        return;
      throw new RuntimeException("namespace still not found after load attempts");
    }
    catch(Exception e)
    {
      e.printStackTrace();
      throw new RuntimeException("Failed to load core namespace: " + coreNamespace, e);
    }

  }

  private String getCoreNamespace()
  {
    final String coreNamespace = System.getProperty("gaeshi.core.namespace");
    if(coreNamespace == null)
      throw new RuntimeException("The 'gaeshi.core.namespace' properties must be defined in appengine-web.xml");
    return coreNamespace;
  }
}
