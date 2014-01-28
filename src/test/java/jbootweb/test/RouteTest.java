package jbootweb.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import jbootweb.flask.App;
import jbootweb.flask.Route;
import jbootweb.util.IO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RouteTest {

  private App app;

  @Route(value = "/hello/:name", method = "GET")
  public String hello(String name) {
    return "Hello " + name;
  }

  @Route(value = "/hello/:name/:surname")
  public String hello2(String name, String surname) {
    return "Hello " + name + " " + surname;
  }

  @Route(value = "/db/hello/:name/stuff")
  public String hello3(String name) {
    return "Hello " + name;
  }

  @Before
  public void setUp() {
    app = new App();
  }

  @After
  public void tearDown() {
    app.destroy();
  }

  @Test
  public void testHelloWorld() throws Exception {
    app.scan(this);
    app.start();

    assertEquals("Hello world", get("/hello/world"));
    assertEquals("Hello world 2", get("/hello/world/2"));
    assertEquals("Hello world", get("/db/hello/world/stuff"));
  }

  private String get(String path) throws MalformedURLException, IOException {
    URL url = new URL("http://localhost:" + app.getPort() + path);
    return new String(IO.readFully(url.openStream()));
  }

}
