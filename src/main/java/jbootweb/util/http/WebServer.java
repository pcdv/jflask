package jbootweb.util.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

/**
 * Wrapper for the HTTP server embedded in the JDK.
 *
 * @see <a
 *      href="http://docs.oracle.com/javase/7/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html">Documentation
 *      for HTTPServer</a>
 * @author pcdv
 */
public class WebServer implements Closeable {

  private final HttpServer srv;

  private final ExecutorService pool;

  private ContentTypeProvider typeProvider = new DefaultContentTypeProvider();

  public WebServer(int port, ExecutorService pool) throws IOException {
    this.srv = HttpServer.create(new InetSocketAddress(port), 0);
    this.pool = pool;
    this.srv.setExecutor(pool);
    this.srv.start();
  }

  public WebServer addHandler(String path, HttpHandler handler) {
    srv.createContext(path, handler);
    return this;
  }

  public void serveStatic(String path, final byte[] data) {
    this.srv.createContext(path, new HttpHandler() {
      public void handle(HttpExchange t) throws IOException {
        t.sendResponseHeaders(200, 0);
        t.getResponseBody().write(data);
        t.getResponseBody().close();
      }
    });
  }

  /**
   * Serves the contents of a given directory from a given root URI.
   *
   * @param rootURI
   * @param dir
   * @return this
   */
  public WebServer serveDir(final String rootURI, final File dir) {
    if (dir == null)
      throw new IllegalArgumentException("Null directory");

    if (dir.exists() && !dir.isDirectory())
      throw new IllegalArgumentException("Not a directory: "
                                         + dir.getAbsolutePath());

    return servePath(rootURI, dir.getAbsolutePath() + File.separator);
  }

  /**
   * Serves the contents of a given path (which may be a directory on the file
   * system or nested in a jar from the classpath) from a given root URI.
   *
   * @param rootURI
   * @param localPath NB: should end with a '/'
   * @return this
   */
  public WebServer servePath(final String rootURI, final String localPath) {
    File file = new File(localPath);
    if (file.exists() && file.isDirectory())
      srv.createContext(rootURI, new FileHandler(this, rootURI, localPath));
    else
      srv.createContext(rootURI, new ResourceHandler(this, rootURI, localPath));
    return this;
  }

  public ContentTypeProvider getContentTypeProvider() {
    return typeProvider;
  }

  /**
   * Shuts down the web sever.
   * <p>
   * WARNING: with JDK6, HttpServer creates a zombie thread (blocked on a
   * sleep()). No problem with JDK 1.7.0_40.
   */
  public void close() {
    this.pool.shutdownNow();
    this.srv.stop(0);
  }

  public int getPort() {
    return srv.getAddress().getPort();
  }

  public void setContentTypeProvider(ContentTypeProvider contentTypeProvider) {
    this.typeProvider = contentTypeProvider;
  }
}
