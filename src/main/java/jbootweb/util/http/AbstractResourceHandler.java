package jbootweb.util.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.FileNotFoundException;
import java.io.InputStream;

import jbootweb.util.IO;
import jbootweb.util.Log;

/**
 * Abstract handler that Serves resources found either in the file system or
 * nested in a jar.
 *
 * @author pcdv
 */
public abstract class AbstractResourceHandler extends DefaultHandler {

  private final String rootURI;

  private final String localPath;

  private final WebServer srv;

  public AbstractResourceHandler(WebServer srv, String rootURI, String localPath) {
    this.srv = srv;
    this.rootURI = rootURI;
    this.localPath = localPath;
  }

  @Override
  public void doGet(HttpExchange t) throws Exception {
    String uri = t.getRequestURI().toString();
    String path = uri.replaceFirst("^" + rootURI, localPath);
    if (path.endsWith("/"))
      path += "index.html";

    int status = 200;
    InputStream in = null;

    try {
      in = openPath(path);
      String contentType = srv.getContentTypeProvider().getContentType(path);
      if (contentType != null)
        t.getResponseHeaders().add("Content-Type", contentType);
    }
    catch (FileNotFoundException e) {
      status = 404;
      Log.error("NOT FOUND: " + uri);
    }
    catch (Exception ex) {
      status = 500;
      Log.error(ex, ex);
    }

    t.sendResponseHeaders(status, 0);
    if (in != null)
      IO.pipe(in, t.getResponseBody(), true);
    else {
      t.getResponseBody().write("Not found".getBytes());
      t.getResponseBody().close();
    }
  }

  protected abstract InputStream openPath(String p)
      throws FileNotFoundException;
}
