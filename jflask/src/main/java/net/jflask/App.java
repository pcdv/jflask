package net.jflask;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.jflask.sun.AbstractResourceHandler;
import net.jflask.sun.ContentTypeProvider;
import net.jflask.sun.DefaultContentTypeProvider;
import net.jflask.sun.FileHandler;
import net.jflask.sun.ResourceHandler;
import net.jflask.sun.WebServer;
import net.jflask.util.Log;

/**
 * Encapsulates the server side of a web app: an HTTP server and some route
 * handlers. There must be only one instance of App in the application. If some
 * route handlers are defined in an external class (i.e. not extending the main
 * App), {@link #scan(Object)} must be called in order to detect them in an
 * instance of the class.
 * <p/>
 * The App can be extended with some handlers:
 * <p/>
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
 * <p/>
 * Or the App can be extended by calling scan():
 * <p/>
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

  private final Map<String, HttpHandler> handlers = new Hashtable<>();

  private ContentTypeProvider mime = new DefaultContentTypeProvider();

  private final ThreadLocal<SunRequest> localRequest = new ThreadLocal<>();

  private final Map<String, ResponseConverter<?>> converters =
      new Hashtable<>();

  private String loginPage;

  private Map<String, Object> sessions = new Hashtable<>();

  private boolean requireLoggedInByDefault;

  private List<MethodHandler> allHandlers = new ArrayList<>(256);

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
   * Scans specified object for route handlers, i.e. public methods with @Route
   * annotation.
   *
   * @see Route
   */
  public void scan(Object obj) {
    for (Method method : obj.getClass().getMethods()) {
      Route route = method.getAnnotation(Route.class);
      if (route != null) {
        addHandler(route, method, obj);
      }
    }
  }

  private void addHandler(Route route, Method m, Object obj) {
    String[] tok = route.value().split("/+");

    // split the static and dynamic part of the route (i.e. /app/hello/:name =>
    // "/app/hello" + "/:name"). The static part is used to get or create a
    // Context, the dynamic part is used to add a handler in the Context.
    StringBuilder root = new StringBuilder(80);
    StringBuilder rest = new StringBuilder(80);
    int i = 0;
    for (; i < tok.length; i++) {
      if (tok[i].isEmpty())
        continue;
      if (tok[i].startsWith(":") || tok[i].startsWith("*"))
        break;
      root.append('/').append(tok[i]);
    }

    for (; i < tok.length; i++) {
      rest.append('/').append(tok[i]);
    }

//    if (rest.length() == 0)
//      rest.append('/');

    MethodHandler handler =
        getContext(root.toString()).addHandler(rest.toString(), route, m, obj);

    allHandlers.add(handler);
  }

  /**
   * Gets or creates a Context for specified root URI.
   */
  private Context getContext(String rootURI) {
    HttpHandler c = handlers.get(rootURI);

    if (c == null) {
      Log.debug("Creating context for " + rootURI);
      handlers.put(rootURI, c = new Context(this, rootURI));
    }
    else if (!(c instanceof Context))
      throw new IllegalStateException("A handler is already registered for: " + rootURI);
    return (Context) c;
  }

  public void addConverter(String name, ResponseConverter<?> conv) {
    converters.put(name, conv);
    reconfigureHandlers();
  }

  public ResponseConverter<?> getConverter(String name) {
    return converters.get(name);
  }

  public void start() throws IOException {
    srv = new WebServer(port, pool);
    for (Entry<String, HttpHandler> e : handlers.entrySet()) {
      String path = e.getKey();
      if (path.isEmpty())
        path = "/";
      srv.addHandler(path, e.getValue());
    }
  }

  public int getPort() {
    // retrieves the actual port even if port 0 was given (for testing)
    if (srv != null)
      return srv.getPort();
    return port;
  }

  public void destroy() {
    srv.close();
  }

  /**
   * Serves the contents of a given path (which may be a directory on the file
   * system or nested in a jar from the classpath) from a given root URI.
   *
   * @param path NB: should end with a '/'
   * @return this
   */
  public App servePath(String rootURI, String path) {
    File file = new File(path);
    AbstractResourceHandler h;
    if (file.exists() && file.isDirectory())
      h = new FileHandler(mime, rootURI, file);
    else
      h = new ResourceHandler(mime, rootURI, path);

    handlers.put(rootURI, h);
    if (srv != null)
      srv.addHandler(rootURI, h);

    return this;
  }

  public App serveDir(String rootURI, File dir) {
    FileHandler h = new FileHandler(mime, rootURI, dir);

    handlers.put(rootURI, h);
    if (srv != null)
      srv.addHandler(rootURI, h);

    return this;
  }

  public void setContentTypeProvider(ContentTypeProvider mime) {
    this.mime = mime;
  }

  void setThreadLocalRequest(SunRequest req) {
    localRequest.set(req);
  }

  public Request getRequest() {
    return localRequest.get();
  }

  public Response getResponse() {
    return localRequest.get();
  }

  /**
   * Returns true if in DEBUG mode. When in debug mode, server stack traces are
   * sent to clients as body of the 500 response.
   */
  public boolean isDebugEnabled() {
    return Log.DEBUG;
  }

  /**
   * Dumps all registered URLs/methods in a readable way into specified buffer.
   * This can be useful to generate reports or to document an API.
   */
  public StringBuilder dumpRoutes(StringBuilder b) {

    ArrayList<Context> contexts = new ArrayList<>();
    for (HttpHandler h : handlers.values()) {
      if (h instanceof Context)
        contexts.add((Context) h);
    }

    Collections.sort(contexts, new Comparator<Context>() {
      public int compare(Context o1, Context o2) {
        return o1.getRootURI().compareTo(o2.getRootURI());
      }
    });

    for (Context c : contexts) {
      c.dumpUrls(b);
      b.append('\n');
    }

    return b;
  }

  /**
   * Marks current session as logged in (by setting a cookie).
   */
  public void loginUser(String login) {
    String token = makeRandomToken(login);
    getResponse().addHeader("Set-Cookie", "sessionToken=" + token);
    sessions.put(token, ""); // TODO: store more useful info about the user
  }

  private String makeRandomToken(String login) {
    return (new Random().nextLong() ^ login.hashCode()) + "";
  }

  /**
   * Replies to current request with an HTTP redirect response with specified
   * location.
   */
  public CustomResponse redirect(String location) {
    Response r = getResponse();
    r.addHeader("Location", location);
    r.setStatus(HttpURLConnection.HTTP_MOVED_TEMP);
    return CustomResponse.INSTANCE;
  }

  /**
   * Checks that the user is currently logged in. This is performed by looking
   * at the "sessionToken" cookie that has been set in session during last
   * call to createSession().
   * <p/>
   * If the user is logged in, the method simply returns true. Otherwise,
   * if the path of the login page has been set using @LoginPage or
   * setLoginPage(), the user is redirected to it. Otherwise a 403 error is
   * returned.
   */
  public boolean checkLoggedIn(HttpExchange r) throws IOException {
    String token = getCookie(r, "sessionToken");
    if (token != null && sessions.containsKey(token)) {
      return true;
    }
    else {
      if (loginPage != null)
        redirect(loginPage);
      else
        r.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, -1);
      return false;
    }
  }

  private String getCookie(HttpExchange r, String name) {
    Headers headers = r.getRequestHeaders();
    if (headers != null) {
      List<String> cookies = headers.get("Cookie");
      if (cookies != null) {
        for (String s : cookies) {
          if (s.startsWith(name) && s.charAt(name.length()) == '=') {
            return s.substring(name.length() + 1);
          }
        }
      }
    }
    return null;
  }

  /**
   * Sets the path of the login page, to which redirect all URLs that require a
   * logged in user. This method can be called directly, or otherwise one of the
   * URL handler methods can be annotated with @LoginPage.
   *
   * @param path the path of the login page
   */
  public void setLoginPage(String path) {
    this.loginPage = path;
  }

  /**
   * Sets the default policy for checking whether user must be logged in to
   * access all URLs by default.
   *
   * @param flag if true, all URL handlers require the user to be logged in
   * except when annotated with @LoginNotRequired. If false, only handlers
   * annotated with @LoginRequired will be protected
   */
  public void setRequireLoggedInByDefault(boolean flag) {
    this.requireLoggedInByDefault = flag;
    reconfigureHandlers();
  }

  /**
   * Returns the default policy for checking whether user must be logged
   * in to access all URLs by default.
   */
  public boolean getRequireLoggedInByDefault() {
    return requireLoggedInByDefault;
  }

  private void reconfigureHandlers() {
    for (MethodHandler h : allHandlers)
      h.configure();
  }

  /**
   * Call this method to destroy the current session, i.e. make the user
   * appearing as "not logged in".
   *
   * @see net.jflask.LoginRequired
   */
  public void logoutUser() {
    HttpExchange x = ((SunRequest) getRequest()).getExchange();
    String token = getCookie(x, "sessionToken");
    if (token != null)
      sessions.remove(token);
  }
}
