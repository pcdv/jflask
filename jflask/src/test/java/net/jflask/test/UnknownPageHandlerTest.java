package net.jflask.test;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import net.jflask.Request;
import net.jflask.Route;
import net.jflask.SunRequest;
import net.jflask.UnknownPageHandler;
import org.junit.Assert;
import org.junit.Test;

public class UnknownPageHandlerTest extends AbstractAppTest {

  @Route("/foo")
  public String foo() {
    return "bar";
  }

  @Route("/")
  public String foo2() {
    return "root";
  }

  @Test
  public void testIt() throws Exception {
    app.setUnknownPageHandler(new UnknownPageHandler() {
      @Override
      public void handle(Request r) throws IOException {
        HttpExchange e = ((SunRequest) r).getExchange();
        e.sendResponseHeaders(200, 0);
        e.getResponseBody().write("gotcha".getBytes());
      }
    });

    Assert.assertEquals("bar", client.get("/foo"));
    Assert.assertEquals("gotcha", client.get("/bar"));
  }
}
