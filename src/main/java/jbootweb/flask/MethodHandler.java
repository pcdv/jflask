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

  public MethodHandler(String uri, String verb, Method m, Object obj) {
    this.verb = verb;
    this.m = m;
    this.obj = obj;

    this.tok = uri.substring(1).split("/");
    this.idx = calcIndexes(tok);
  }

  private static int[] calcIndexes(String[] tok) {
    int[] res = new int[tok.length];
    int j = 0;
    for (int i = 0; i < tok.length; i++) {
      if (tok[i].charAt(0) == ':')
        res[j++] = i;
    }
    return Arrays.copyOf(res, j);
  }

  public boolean handle(HttpExchange r, String[] uri) throws Exception {
    if (!r.getRequestMethod().equals(this.verb))
      return false;

    if (uri.length != tok.length)
      return false;

    for (int i = 0; i < uri.length; i++) {
      if (tok[i].charAt(0) != ':' && !tok[i].equals(uri[i]))
        return false;
    }

    Object[] args = new Object[idx.length];
    for (int i = 0; i < args.length; i++) {
      args[i] = uri[idx[i]];
    }

    String s = (String) m.invoke(obj, args);
    r.sendResponseHeaders(200, 0);
    r.getResponseBody().write(s.getBytes("UTF-8"));

    return true;
  }
}
