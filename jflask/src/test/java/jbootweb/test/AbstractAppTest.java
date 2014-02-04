package jbootweb.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import jbootweb.flask.App;
import jbootweb.util.IO;

import org.junit.After;
import org.junit.Before;

public class AbstractAppTest {

  protected App app;

  @Before
  public void setUp() throws IOException {
    app = new App();
    app.setPort(0);
    app.scan(this);
    app.start();
  }

  @After
  public void tearDown() {
    app.destroy();
  }

  protected String get(String path) throws MalformedURLException, IOException {
    URL url = new URL("http://localhost:" + app.getPort() + path);
    return new String(IO.readFully(url.openStream()));
  }

}
