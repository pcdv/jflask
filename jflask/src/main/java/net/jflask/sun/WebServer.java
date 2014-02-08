package net.jflask.sun;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Wrapper for the HTTP server embedded in the JDK.
 *
 * @see <a
 *      href="http://docs.oracle.com/javase/7/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html">Documentation
 *      for HTTPServer</a>
 * @author pcdv
 */
public class WebServer  {

  private final HttpServer srv;

  private final ExecutorService pool;

  public WebServer(int port, ExecutorService pool) throws IOException {
    if (pool == null)
      pool = Executors.newCachedThreadPool();
    this.srv = HttpServer.create(new InetSocketAddress(port), 0);
    this.pool = pool;
    this.srv.setExecutor(pool);
    this.srv.start();
  }

  public WebServer addHandler(String path, HttpHandler handler) {
    srv.createContext(path, handler);
    return this;
  }

  /**
   * Shuts down the web sever.
   * <p>
   * WARNING: with JDK6, HttpServer creates a zombie thread (blocked on a
   * sleep()). No problem with JDK 1.7.0_40.
   */
  public void close() {
    this.srv.stop(0);
    pool.shutdownNow();
  }

  public int getPort() {
    return srv.getAddress().getPort();
  }
}
