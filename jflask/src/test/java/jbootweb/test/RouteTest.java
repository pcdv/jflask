package jbootweb.test;

import static org.junit.Assert.*;
import jbootweb.flask.Route;

import org.junit.Test;

public class RouteTest extends AbstractAppTest {

  @Route(value = "/hello/:name", method = "GET")
  public String hello(String name) {
    return "Hello " + name;
  }

  @Route("/hello/:name/:surname")
  public String hello2(String name, String surname) {
    return "Hello " + name + " " + surname;
  }

  @Route("/db/hello/:name/stuff")
  public String hello3(String name) {
    return "Hello " + name;
  }

  @Test
  public void testHelloWorld() throws Exception {
    assertEquals("Hello world", get("/hello/world"));
    assertEquals("Hello world 2", get("/hello/world/2"));
    assertEquals("Hello world", get("/db/hello/world/stuff"));
  }
}
