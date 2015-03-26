package net.jflask;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.sun.net.httpserver.HttpExchange;
import net.jflask.util.IO;
import net.jflask.util.Log;

/**
 * Handles a request submitted by the Context, if compatible with the HTTP
 * method and URI schema.
 *
 * @author pcdv
 */
public class MethodHandler implements Comparable<MethodHandler> {

  private static final String[] EMPTY = {};

  /** The HTTP method */
  private final String verb;

  /** The method to invoke to process request. */
  private final Method m;

  /** The object to invoke method on. */
  private final Object obj;

  /** The split URI, eg. { "hello", ":name" } */
  private final String[] tok;

  /**
   * The indexes of variables in split URI, eg. { 1 } to extract "world" from
   * "/hello/world" if URI schema is "/hello/:name"
   */
  private final int[] idx;

  private boolean loginRequired;

  private int splat = -1;

  private final String rootURI;

  private final Route route;

  private final Context ctx;

  @SuppressWarnings("rawtypes")
  private ResponseConverter converter;

  private final String uri;

  public MethodHandler(Context ctx,
                       String uri,
                       Method m,
                       Object obj,
                       Route route) {
    this.ctx = ctx;
    this.uri = uri;
    this.rootURI = uri;
    this.verb = route.method();
    this.m = m;
    this.obj = obj;
    this.route = route;
    this.tok = uri.isEmpty() ? EMPTY : uri.substring(1).split("/");
    this.idx = calcIndexes(tok);

    if (m.getDeclaredAnnotation(LoginPage.class) != null)
      ctx.app.setLoginPage(ctx.getRootURI() + uri);

    configure();

    // hack for being able to call method even if not public or if the class
    // is not public
    if (!m.isAccessible())
      m.setAccessible(true);

    for (Class<?> c : m.getParameterTypes()) {
      if (c != String.class)
        throw new RuntimeException//
                  ("Only String supported in method arguments (for now): " + m);
    }
  }

  /**
   * Called during construction and when App configuration changes that may
   * require adaptation in handlers.
   */
  public void configure() {
    if (m.getDeclaredAnnotation(LoginRequired.class) != null)
      loginRequired = true;
    else if (m.getDeclaredAnnotation(LoginNotRequired.class) != null || m.getDeclaredAnnotation(LoginPage.class) != null)
      loginRequired = false;
    else
      loginRequired = ctx.app.getRequireLoggedInByDefault();

    if (this.converter == null && !route.converter().isEmpty())
      this.converter = ctx.app.getConverter(route.converter());
  }

  private int[] calcIndexes(String[] tok) {
    int[] res = new int[tok.length];
    int j = 0;
    for (int i = 0; i < tok.length; i++) {
      if (tok[i].charAt(0) == ':') {
        if (splat != -1)
          throw new IllegalArgumentException("Invalid route: " + rootURI);
        res[j++] = i;
      }
      if (tok[i].charAt(0) == '*') {
        if (i != tok.length - 1)
          throw new IllegalArgumentException("Invalid route: " + rootURI);
        res[j++] = i;
        splat = i;
      }
    }
    return Arrays.copyOf(res, j);
  }

  @SuppressWarnings("unchecked")
  public boolean handle(HttpExchange r,
                        String[] uri,
                        Response resp) throws Exception {

    if (!isApplicable(r, uri))
      return false;

    if (loginRequired && !ctx.app.checkLoggedIn(r)) {
      return true;
    }

    String[] args = extractArgs(uri);

    if (Log.DEBUG)
      Log.debug("Invoking " + obj.getClass()
                                 .getSimpleName() + "." + m.getName() + Arrays.toString(args));

    return processResponse(r, resp, m.invoke(obj, args));
  }

  private boolean processResponse(HttpExchange r,
                                  Response resp,
                                  Object res) throws Exception {
    if (converter != null) {
      converter.convert(res, resp);
    }
    else if (!route.converter().isEmpty()) {
      throw new IllegalStateException("Converter " + route.converter() + " not registered in App.");
    }
    else if (res instanceof CustomResponse) {
      // do nothing: status and headers should already be set
    }
    else if (res instanceof String) {
      r.sendResponseHeaders(200, 0);
      r.getResponseBody().write(((String) res).getBytes("UTF-8"));
    }
    else if (res instanceof byte[]) {
      r.sendResponseHeaders(200, 0);
      r.getResponseBody().write((byte[]) res);
    }
    else if (res instanceof InputStream) {
      r.sendResponseHeaders(200, 0);
      IO.pipe((InputStream) res, r.getResponseBody(), false);
    }
    else
      throw new RuntimeException("Unexpected return value: " + res);

    return true;
  }

  /**
   * Computes the list of arguments to pass to the decorated method.
   */
  private String[] extractArgs(String[] uri) {
    String[] args = new String[idx.length];
    for (int i = 0; i < args.length; i++) {
      args[i] = uri[idx[i]];
    }
    if (splat != -1) {
      for (int i = splat + 1; i < uri.length; i++) {
        args[args.length - 1] += "/" + uri[i];
      }
    }
    return args;
  }

  /**
   * Checks whether current handler should respond to specified request.
   */
  private boolean isApplicable(HttpExchange r, String[] uri) {
    if (!r.getRequestMethod().equals(this.verb))
      return false;

    if (uri.length != tok.length) {
      if (splat == -1 || uri.length < tok.length)
        return false;
    }

    for (int i = 0; i < tok.length; i++) {
      if (tok[i].charAt(0) != ':' && tok[i].charAt(0) != '*' && !tok[i].equals(uri[i]))
        return false;
    }

    return true;
  }

  public int compareTo(MethodHandler o) {
    if (Arrays.equals(tok, o.tok))
      return verb.compareTo(o.verb);
    return uri.compareTo(o.uri);
  }

  public String getURI() {
    return uri;
  }

  public String getVerb() {
    return verb;
  }

  public Method getMethod() {
    return m;
  }

}
