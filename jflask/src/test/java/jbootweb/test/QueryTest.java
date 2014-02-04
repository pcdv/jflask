package jbootweb.test;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import jbootweb.flask.Route;

public class QueryTest extends AbstractAppTest {

  @Route("/hello/:name")
  public String hello(String name) {
    return "Hello " + name;
  }

  @Route("/hello2")
  public String helloQuery() {
    return "Hello " + app.getRequest().getArg("name", null);
  }

  @Route("/hello_bytearray")
  public byte[] helloByteArray() {
    return ("Hello " + app.getRequest().getArg("name", null)).getBytes();
  }

  @Test
  public void testTrimQS() throws Exception {
    assertEquals("Hello world", get("/hello/world?foo=bar"));
  }

  @Test
  public void testGetArg() throws Exception {
    assertEquals("Hello world", get("/hello2?name=world"));
  }

  @Test
  @Ignore
  public void testGetArgSlash() throws Exception {
    assertEquals("Hello world", get("/hello2/?name=world"));
  }

  @Test
  public void testReturnByteArray() throws Exception {
    assertEquals("Hello world", get("/hello_bytearray?name=world"));
  }

}
