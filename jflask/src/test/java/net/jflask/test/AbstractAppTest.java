package net.jflask.test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import net.jflask.App;
import net.jflask.util.IO;
import org.junit.After;
import org.junit.Before;

public class AbstractAppTest {

  protected App app;

  @Before
  public void setUp() throws IOException {
    app = new App();
    app.setPort(0);

    preScan();
    app.scan(this);

    preStart();
    app.start();
  }

  /**
   * Override this method to execute code before the app scans request
   * handlers.
   */
  protected void preScan() {
  }

  /**
   * Override this method to execute code before the app is started.
   */
  protected void preStart() {
  }

  @After
  public void tearDown() {
    app.destroy();
  }

  /**
   * GETs data from the server at specified path.
   */
  public String get(String path) throws IOException {
    URL url = new URL("http://localhost:" + app.getPort() + path);
    return new String(IO.readFully(url.openStream()));
  }

  /**
   * POSTs data on the server at specified path and return results as string.
   */
  public String post(String path, String data) throws IOException {
    URL url = new URL("http://localhost:" + app.getPort() + path);
    URLConnection con = url.openConnection();
    con.setDoOutput(true);
    con.getOutputStream().write(data.toString().getBytes());
    con.getOutputStream().close();
    return new String(IO.readFully(con.getInputStream()));
  }

}
