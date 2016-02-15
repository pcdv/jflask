package net.jflask.test;

import net.jflask.Convert;
import net.jflask.Response;
import net.jflask.ResponseConverter;
import net.jflask.Route;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConverterTest extends AbstractAppTest {

  @Convert("STAR")
  @Route("/hello/:name")
  public String hello(String name) {
    return "Hello " + name;
  }

  @Override
  protected void preScan() {
    app.addConverter("FOO", new ResponseConverter<String>() {
      public void convert(String data, Response resp) throws Exception {
        resp.setStatus(200);
        resp.getOutputStream().write(("FOO " + data).getBytes());
      }
    });
  }

  @Route(value = "/hello2/:name", converter = "FOO")
  public String hello2(String name) {
    return "Hello " + name;
  }

  @Test
  public void testConverterAddedAfterStart() throws Exception {
    app.addConverter("STAR", new ResponseConverter<String>() {
      public void convert(String data, Response resp) throws Exception {
        resp.setStatus(200);
        resp.getOutputStream().write(("*" + data + "*").getBytes());
      }
    });
    assertEquals("*Hello world*", client.get("/hello/world?foo=bar"));
  }

  @Test
  public void testConverterAddedBeforeScan() throws Exception {
    assertEquals("FOO Hello world", client.get("/hello2/world"));
  }
}
