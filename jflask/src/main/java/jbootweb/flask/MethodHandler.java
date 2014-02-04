package jbootweb.flask;

import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Handles a request submitted by the Context, if compatible with the HTTP
 * method and URI schema.
 *
 * @author pcdv
 */
public class MethodHandler {

  private static final String[] EMPTY = {};

  /** HTTP method */
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

  private int splat = -1;

  private final String rootURI;

  public MethodHandler(String uri, String verb, Method m, Object obj) {
    this.rootURI = uri;
    this.verb = verb;
    this.m = m;
    this.obj = obj;

    this.tok = uri.isEmpty() ? EMPTY :uri.substring(1).split("/");
    this.idx = calcIndexes(tok);

    // hack for being able to call method even if not public or if the class
    // is not public
    if (!m.isAccessible())
      m.setAccessible(true);
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

  public boolean handle(HttpExchange r, String[] uri) throws Exception {
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

    Object[] args = new Object[idx.length];
    for (int i = 0; i < args.length; i++) {
      args[i] = uri[idx[i]];
    }
    if (splat != -1) {
      for (int i = splat + 1; i < uri.length; i++) {
        args[splat] += "/" + uri[i];
      }
    }

    String s = (String) m.invoke(obj, args);
    r.sendResponseHeaders(200, 0);
    r.getResponseBody().write(s.getBytes("UTF-8"));

    return true;
  }
}
