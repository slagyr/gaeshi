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
  protected Exception loadError;
  protected Thread loadThread;

  public GaeshiServlet()
  {
    loadThread = new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          loadServiceMethod();
        }
        catch(Exception e)
        {
          loadError = e;
        }
      }
    });
    loadThread.start();
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    waitForLoad();
    invokeServiceMethod(req, resp);
  }

  protected void waitForLoad()
  {
    if(loadThread.isAlive())
    {
      try
      {
        loadThread.join();
      }
      catch(InterruptedException e)
      {
        throw new RuntimeException(e);
      }
    }
    else if(loadError != null)
      throw new RuntimeException("Failed to load Service Method", loadError);
  }

  private void invokeServiceMethod(HttpServletRequest req, HttpServletResponse resp) throws ServletException
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
    final String coreFilename = Clj.nsToFilename(coreNamespace);
    try
    {
      final Symbol nsSymbol = Symbol.intern(null, coreNamespace);
      final Namespace ns = Namespace.find(nsSymbol);
      if(ns == null)
        RT.loadResourceScript(coreFilename);
    }
    catch(Exception e)
    {
      throw new RuntimeException("Failed to load core src file: " + coreFilename, e);
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