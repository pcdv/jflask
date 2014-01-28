package jbootweb.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import jbootweb.flask.App;
import jbootweb.flask.Route;
import jbootweb.util.IO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RouteTestExtendsApp extends App {

  @Route(value = "/hello/:name")
  public String hello(String name) {
    return "Hello " + name;
  }

  @Before
  public void setUp() throws IOException {
    start();
  }

  @After
  public void tearDown() {
    destroy();
  }

  @Test
  public void testHelloWorld() throws Exception {
    assertEquals("Hello world", get("/hello/world"));
  }

  private String get(String path) throws Exception {
    URL url = new URL("http://localhost:" + getPort() + path);
    return new String(IO.readFully(url.openStream()));
  }

}
