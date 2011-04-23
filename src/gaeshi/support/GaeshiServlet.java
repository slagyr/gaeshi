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
      makeServiceMethodFn = loadVar("gaeshi.support.servlet", "make-service-method");
    return makeServiceMethodFn;
  }

  protected void loadServiceMethod() throws Exception
  {
    final String coreNamespace = getCoreNamespace();
    Var handler = loadHandler(coreNamespace);

    serviceMethod = (IFn) getMakeServiceMethodFn().invoke(handler);
  }

  private Var loadHandler(String coreNamespace)
  {
    Var handler = null;
    try
    {
      handler = loadVar(coreNamespace, "gaeshi-handler");
      if(!(handler.deref() instanceof IFn))
        throw new Exception("Not an IFn");
    }
    catch(Exception e)
    {
      throw new RuntimeException(coreNamespace + "/gaeshi-handler must define a Ring handler", e);
    }
    return handler;
  }

  protected static Var loadVar(String namespace, String varName)
  {
    try
    {
      Symbol namespaceSymbol = Symbol.intern(namespace);
      Namespace ns = Namespace.find(namespaceSymbol);
      if(ns != null)
        return (Var)ns.getMapping(Symbol.create(varName));

      System.err.println("ATTEMPTING TO LOAD NAMESPACE " + namespace + "/" + varName);
      RT.load(namespace, false);

      ns = Namespace.find(namespaceSymbol);
      if(ns != null)
        return (Var)ns.getMapping(Symbol.create(varName));

      System.err.println("ATTEMPTING TO LOAD RESOURCE SCRIPT " + namespace + "/" + varName);
      final String coreFilename = Clj.nsToFilename(namespace);
      RT.loadResourceScript(coreFilename);
      ns = Namespace.find(namespaceSymbol);
      if(ns != null)
        return (Var)ns.getMapping(Symbol.create(varName));

      throw new RuntimeException("var still not found after load attempts: " + namespace + "/" + varName);
    }
    catch(Exception e)
    {
      e.printStackTrace();
      throw new RuntimeException("Failed to load var:" + namespace + "/" + varName, e);
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
