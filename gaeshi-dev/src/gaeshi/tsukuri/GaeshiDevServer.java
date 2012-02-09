package gaeshi.tsukuri;

import com.google.apphosting.utils.config.AppEngineWebXml;
import com.google.apphosting.utils.config.AppEngineWebXmlReader;
import mmargs.Arguments;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.log.StdErrLog;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GaeshiDevServer
{
  public static Map<String, String> servlets = new HashMap<String, String>();
  static
  {
    servlets.put("/_ah/admin", "com.google.apphosting.utils.servlet.DatastoreViewerServlet");
    servlets.put("/_ah/admin/", "com.google.apphosting.utils.servlet.DatastoreViewerServlet");
    servlets.put("/_ah/adminConsole", "org.apache.jsp.ah.adminConsole_jsp");
    servlets.put("/_ah/admin/backends", "com.google.apphosting.utils.servlet.ServersServlet");
    servlets.put("/_ah/admin/datastore", "com.google.apphosting.utils.servlet.DatastoreViewerServlet");
    servlets.put("/_ah/admin/inboundmail", "com.google.apphosting.utils.servlet.InboundMailServlet");
    servlets.put("/_ah/admin/taskqueue", "com.google.apphosting.utils.servlet.TaskQueueViewerServlet");
    servlets.put("/_ah/admin/xmpp", "com.google.apphosting.utils.servlet.XmppServlet");
    servlets.put("/_ah/backendsBody", "org.apache.jsp.ah.backendsBody_jsp");
    servlets.put("/_ah/backendsFinal", "org.apache.jsp.ah.backendsFinal_jsp");
    servlets.put("/_ah/backendsHead", "org.apache.jsp.ah.backendsHead_jsp");
    servlets.put("/_ah/channel/dev", "com.google.appengine.api.channel.dev.LocalChannelServlet");
    servlets.put("/_ah/channel/jsapi", "com.google.appengine.api.channel.dev.ServeScriptServlet");
    servlets.put("/_ah/datastoreViewer", "com.google.apphosting.utils.servlet.DatastoreViewerServlet");
    servlets.put("/_ah/datastoreViewerBody", "org.apache.jsp.ah.datastoreViewerBody_jsp");
    servlets.put("/_ah/datastoreViewerFinal", "org.apache.jsp.ah.datastoreViewerFinal_jsp");
    servlets.put("/_ah/datastoreViewerHead", "org.apache.jsp.ah.datastoreViewerHead_jsp");
    servlets.put("/_ah/default", "com.google.appengine.tools.development.LocalResourceFileServlet");
    servlets.put("/_ah/entityDetailsBody", "org.apache.jsp.ah.entityDetailsBody_jsp");
    servlets.put("/_ah/entityDetailsFinal", "org.apache.jsp.ah.entityDetailsFinal_jsp");
    servlets.put("/_ah/entityDetailsHead", "org.apache.jsp.ah.entityDetailsHead_jsp");
    servlets.put("/_ah/img/*", "com.google.appengine.api.images.dev.LocalBlobImageServlet");
    servlets.put("/_ah/inboundMail", "com.google.apphosting.utils.servlet.InboundMailServlet");
    servlets.put("/_ah/inboundMailBody", "org.apache.jsp.ah.inboundMailBody_jsp");
    servlets.put("/_ah/inboundMailFinal", "org.apache.jsp.ah.inboundMailFinal_jsp");
    servlets.put("/_ah/inboundMailHead", "org.apache.jsp.ah.inboundMailHead_jsp");
    servlets.put("/_ah/login", "com.google.appengine.api.users.dev.LocalLoginServlet");
    servlets.put("/_ah/logout", "com.google.appengine.api.users.dev.LocalLogoutServlet");
    servlets.put("/_ah/OAuthAuthorizeToken", "com.google.appengine.api.users.dev.LocalOAuthAuthorizeTokenServlet");
    servlets.put("/_ah/OAuthGetAccessToken", "com.google.appengine.api.users.dev.LocalOAuthAccessTokenServlet");
    servlets.put("/_ah/OAuthGetRequestToken", "com.google.appengine.api.users.dev.LocalOAuthRequestTokenServlet");
    servlets.put("/_ah/queue_deferred", "com.google.apphosting.utils.servlet.DeferredTaskServlet");
    servlets.put("/_ah/resources", "com.google.apphosting.utils.servlet.AdminConsoleResourceServlet");
    servlets.put("/_ah/sessioncleanup", "com.google.apphosting.utils.servlet.SessionCleanupServlet");
    servlets.put("/_ah/taskqueueViewerBody", "org.apache.jsp.ah.taskqueueViewerBody_jsp");
    servlets.put("/_ah/taskqueueViewerFinal", "org.apache.jsp.ah.taskqueueViewerFinal_jsp");
    servlets.put("/_ah/taskqueueViewerHead", "org.apache.jsp.ah.taskqueueViewerHead_jsp");
    servlets.put("/_ah/upload/*", "com.google.appengine.api.blobstore.dev.UploadBlobServlet");
    servlets.put("/_ah/xmppBody", "org.apache.jsp.ah.xmppBody_jsp");
    servlets.put("/_ah/xmppFinal", "org.apache.jsp.ah.xmppFinal_jsp");
    servlets.put("/_ah/xmppHead", "org.apache.jsp.ah.xmppHead_jsp");
  }

  private static Arguments argSpec = new Arguments();
  static
  {
    argSpec.addValueOption("p", "port", "PORT", "Change the port (default: 8080)");
    argSpec.addValueOption("a", "address", "ADDRESS", "Change the address (default: 127.0.0.1)");
    argSpec.addValueOption("e", "environment", "ENVIRONMENT", "Change the environment (default: development)");
    argSpec.addValueOption("d", "directory", "DIRECTORY", "Change the directory (default: .)");
  }

  private AppEngineWebXml appEngineWebXml;
  private static Logger log = Logger.getLogger(GaeshiDevServer.class.getName());
  private GaeshiDevServerEnvironment env;

  public static void main(String[] args) throws Exception
  {
    enableConsoleLogging();
    GaeshiDevServer gaeshiDevServer = new GaeshiDevServer();
    gaeshiDevServer.parseArgs(args);
    gaeshiDevServer.start();
  }

  public GaeshiDevServer()
  {
    env = new GaeshiDevServerEnvironment();
    env.install();
  }

  public GaeshiDevServerEnvironment getEnv()
  {
    return env;
  }

  public void parseArgs(String[] args)
  {
    final Map<String, Object> options = argSpec.parse(args);
    final Object errors = options.get("*errors");
    if(errors != null)
    {
      System.out.println("Usage: lein server " + argSpec.argString());
      System.out.println(argSpec.optionsString());
      System.exit(-1);
    }
    else
    {
      if(options.containsKey("port"))
        env.port = Integer.parseInt((String)options.get("port"));
      if(options.containsKey("address"))
        env.address = (String)options.get("address");
      if(options.containsKey("environment"))
        env.env = (String)options.get("environment");
      if(options.containsKey("directory"))
        env.dir = (String)options.get("directory");
    }
  }

  private void start() throws Exception
  {
    Server server = new Server(env.port);
    WebAppContext context = new WebAppContext(env.dir, "/");

    applyAppEngineWebXml(context);
    applyWebXml(context);
    addFilters(context);
    addAppEngineServlets(context);

    server.setHandler(context);
    log.info(env.toString() + " starting up ...");
    server.start();
    log.info(env.toString() + " is up and running.");
    server.join();
  }

  private static void enableConsoleLogging()
  {
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.ALL);
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).addHandler(new ConsoleHandler());
    Log.setLog(new StdErrLog());
//    Log.getLog().setDebugEnabled(true);
  }

  private void applyWebXml(WebAppContext context)
  {
    context.setDescriptor("config/" + env.env + "/web.xml");
  }

  private void applyAppEngineWebXml(WebAppContext context)
  {
    AppEngineWebXmlReader appWebXmlReader = new AppEngineWebXmlReader(env.dir, "../config/" + env.env + "/appengine-web.xml");
    appEngineWebXml = appWebXmlReader.readAppEngineWebXml();
    context.setAttribute("com.google.appengine.tools.development.appEngineWebXml", appEngineWebXml);
    System.getProperties().putAll(this.appEngineWebXml.getSystemProperties());
  }

  private void addAppEngineServlets(WebAppContext context)
  {
    for(Map.Entry<String, String> servletEntry : servlets.entrySet())
      context.addServlet(servletEntry.getValue(), servletEntry.getKey());
  }

  private void addFilters(WebAppContext context)
  {
      context.addFilter("com.google.appengine.tools.development.StaticFileFilter", "/*", Handler.ALL);
      context.addFilter(new FilterHolder(new GaeshiRequestEnvFilter(appEngineWebXml)), "/*", Handler.ALL);
      context.addFilter("com.google.apphosting.utils.servlet.TransactionCleanupFilter", "/*", Handler.ALL);
      context.addFilter("com.google.appengine.api.blobstore.dev.ServeBlobFilter", "/*", Handler.ALL);
  }

}
