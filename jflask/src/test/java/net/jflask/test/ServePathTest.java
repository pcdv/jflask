package net.jflask.test;

import net.jflask.App;
import net.jflask.Route;
import net.jflask.sun.WebServer;
import net.jflask.test.util.SimpleClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * Reproduce bug with handler registered twice in server when server started
 * before app.
 */
public class ServePathTest {

  @Route("/test")
  public String test() {
    return "OK";
  }

  @Test
  public void testStartServerBeforeServePath() throws Exception {
    WebServer ws = new WebServer(0, null);
    ws.start();
    App app = new App("/app", ws);
    app.servePath("/static", "/test-resources");
    app.start();

    SimpleClient client = new SimpleClient(ws);
    Assert.assertEquals("FOO", client.get("/app/static/foo.html"));
  }

  @Test
  public void testRedirectLoginToResource() throws Exception {
    WebServer ws = new WebServer(0, null);
    ws.start();
    App app = new App("/app", ws);
    app.servePath("/static", "/test-resources");
    app.setRequireLoggedInByDefault(true);
    app.setLoginPage("/static/login.html");
    app.scan(this);
    app.start();

    SimpleClient client = new SimpleClient(ws);
    Assert.assertEquals("Please login", client.get("/app/test"));
  }
}
