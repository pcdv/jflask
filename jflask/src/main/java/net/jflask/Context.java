package net.jflask;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jflask.util.Log;

/**
 * A HTTP handler that receives all requests on a given rootURI and dispatches
 * them to configured route handlers.
 *
 * @author pcdv
 */
public class Context implements HttpHandler {

  private static final String[] EMPTY = {};

  private final String rootURI;

  private final List<MethodHandler> handlers = new ArrayList<>();

  final App app;

  public Context(App app, String rootURI) {
    this.app = app;
    this.rootURI = rootURI;
  }

  /**
   * Registers a java method that must be called to process requests matchinig
   * specified URI (relative to rootURI).
   *
   * @param uri URI schema relative to rootURI (eg. "/:name")
   * @param verb a HTTP method (GET, POST, ...)
   * @param m a java method
   * @param obj the object on which the method must be invoked
   */
  public void addHandler(String uri, Route route, Method m, Object obj) {
    Log.debug("Add handler for " + route.method() + " on " + rootURI + uri);
    handlers.add(new MethodHandler(this, uri, m, obj, route));
  }

  public String getRootURI() {
    return rootURI;
  }

  public void handle(HttpExchange r) throws IOException {
    SunRequest req = new SunRequest(r);
    app.setThreadLocalRequest(req);
    String uri = req.getRequestURI().substring(rootURI.length());
    try {
      String[] tok = uri.isEmpty() ? EMPTY : uri.substring(1).split("/");
      for (MethodHandler h : handlers) {
        if (h.handle(r, tok, req, req)) {
          return;
        }
      }
      Log.warn("No handler found for: "
               + r.getRequestMethod()
               + " "
               + req.getRequestURI());

      r.sendResponseHeaders(404, 0);
    }
    catch (Exception ex) {
      Log.error(ex, ex);
      r.sendResponseHeaders(500, 0);
      if (app.isDebugEnabled()) {
        ex.printStackTrace(new PrintStream(r.getResponseBody()));
      }
    }
    finally {
      r.getResponseBody().close();
    }
  }

  public void onConverterAdd(String name, ResponseConverter<?> conv) {
    for (MethodHandler h : handlers)
      h.onConverterAdd();
  }

  public void dumpUrls(StringBuilder b) {
    b.append(rootURI).append(":\n");

    ArrayList<MethodHandler> list = new ArrayList<>(handlers);
    Collections.sort(list);

    for (MethodHandler mh : list) {
      b.append(String.format("%-50s  %-8s  %-15s %s\n",
                             rootURI + mh.getURI(),
                             mh.getVerb(),
                             mh.getMethod().getName(),
                             mh.getMethod().getDeclaringClass().getName()));
    }
  }
}
