package net.jflask.sun;

import com.sun.net.httpserver.HttpExchange;

import java.io.FileNotFoundException;
import java.io.InputStream;

import net.jflask.util.IO;
import net.jflask.util.Log;

/**
 * Abstract handler that Serves resources found either in the file system or
 * nested in a jar.
 *
 * @author pcdv
 */
public abstract class AbstractResourceHandler extends DefaultHandler {

  protected final String rootURI;

  private final ContentTypeProvider mime;

  public AbstractResourceHandler(ContentTypeProvider mime,
                                 String rootURI) {
    this.mime = mime;
    this.rootURI = rootURI;
  }

  @Override
  public void doGet(HttpExchange t) throws Exception {
    String uri = t.getRequestURI().toString();
    if (uri.endsWith("/"))
      uri += "index.html";
    String path = uri.replaceFirst("^" + rootURI, "");

    int status = 200;
    InputStream in = null;

    try {
      in = openPath(path);
      String contentType = mime.getContentType(path);
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
