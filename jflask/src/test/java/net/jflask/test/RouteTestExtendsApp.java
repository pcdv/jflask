package net.jflask.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import net.jflask.App;
import net.jflask.Route;
import net.jflask.util.IO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Example showing an extended App.
 */
public class RouteTestExtendsApp extends App {

  @Route("/hello/:name")
  public String hello(String name) {
    return "Hello " + name;
  }

  @Route("/hello/foo/*bar")
  public String helloSplat(String bar) {
    return "Hello " + bar;
  }

  @Before
  public void setUp() throws IOException {
    srv.setPort(0); // use any available port instead of 8080
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

  @Test
  public void testHelloSplat() throws Exception {
    assertEquals("Hello a/b/c", get("/hello/foo/a/b/c"));
  }

  @Test
  public void testHelloSplat1() throws Exception {
    assertEquals("Hello a", get("/hello/foo/a"));
  }

  private String get(String path) throws Exception {
    URL url = new URL("http://localhost:" + getPort() + path);
    return new String(IO.readFully(url.openStream()));
  }
}
