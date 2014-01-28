package jbootweb.flask;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import jbootweb.util.Log;
import jbootweb.util.http.WebServer;

/**
 * Encapsulates the server side of a web app: an HTTP server and some route
 * handlers.
 * <p>
 * The App can be extended with some handlers:
 *
 * <pre>
 * public class MyApp extends App {
 *   &#064;Route(value = &quot;/hello/:name&quot;)
 *   public String hello(String name) {
 *     return &quot;Hello &quot; + name;
 *   }
 * }
 * ...
 * new MyApp().start()
 * </pre>
 *
 * Or the App can be extended by calling scan():
 *
 * <pre>
 * public class MyApp {
 *   &#064;Route(value = &quot;/hello/:name&quot;)
 *   public String hello(String name) {
 *     return &quot;Hello &quot; + name;
 *   }
 * }
 * ...
 * App app = new App()
 * app.scan(new MyApp());
 * app.start();
 * </pre>
 *
 * @author pcdv
 */
public class App {

  private int port = 8080;

  private ExecutorService pool;

  private WebServer srv;

  private final Map<String, Context> contexts = new Hashtable<>();

  public App() {
    // in case we are extended by a subclass with annotations
    scan(this);
  }

  public void setPort(int port) {
    checkNotStarted();
    this.port = port;
  }

  public void setExecutorService(ExecutorService pool) {
    checkNotStarted();
    this.pool = pool;
  }

  private void checkNotStarted() {
    if (srv != null)
      throw new IllegalStateException("Already started");
  }

  /**
   * Scans specified object for route handlers.
   *
   * @param obj
   * @see Route
   */
  public void scan(Object obj) {
    for (Method method : obj.getClass().getMethods()) {
      Route ann = method.getAnnotation(Route.class);
      if (ann != null) {
        String route = ann.value();
        String verb = ann.method();
        addHandler(route, verb, method, obj);
      }
    }
  }

  private void addHandler(String route, String verb, Method m, Object obj) {
    String[] tok = route.split("/+");

    // split the static and dynamic part of the route (i.e. /app/hello/:name =>
    // "/app/hello" + "/:name")
    StringBuilder root = new StringBuilder(80);
    StringBuilder rest = new StringBuilder(80);
    int i = 0;
    for (; i < tok.length; i++) {
      if (tok[i].isEmpty())
        continue;
      if (tok[i].startsWith(":"))
        break;
      root.append('/').append(tok[i]);
    }

    for (; i < tok.length; i++) {
      rest.append('/').append(tok[i]);
    }

    getContext(root.toString()).addHandler(rest.toString(), verb, m, obj);
  }

  /**
   * Gets or creates a Context for specified root URI.
   */
  private Context getContext(String rootURI) {
    Context c = contexts.get(rootURI);
    if (c == null) {
      Log.info("Creating context for " + rootURI);
      contexts.put(rootURI, c = new Context(rootURI));
    }
    return c;
  }

  public void start() throws IOException {
    srv = new WebServer(port, pool);
    for (Context c : contexts.values())
      srv.addHandler(c.getRootURI(), c);
  }

  public int getPort() {
    return port;
  }

  public void destroy() {
    srv.close();
  }

}
