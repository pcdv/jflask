package net.jflask.test;

import net.jflask.App;
import net.jflask.Route;
import net.jflask.sun.WebServer;
import net.jflask.test.util.SimpleClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class ServePathTest {

  private App app;

  @After
  public void tearDown() throws Exception {
    app.destroy();
  }

  @Route("/test")
  public String test() {
    return "OK";
  }

  /**
   * Reproduce bug with handler registered twice in server when server started
   * before app.
   */
  @Test
  public void testStartServerBeforeServePath() throws Exception {
    WebServer ws = new WebServer(0, null);
    ws.start();
    app = new App("/app", ws);
    app.servePath("/static", "/test-resources");
    app.start();

    SimpleClient client = new SimpleClient(ws);
    Assert.assertEquals("FOO", client.get("/app/static/foo.html"));
  }

  @Test
  public void testRedirectLoginToResource() throws Exception {
    WebServer ws = new WebServer(0, null);
    ws.start();
    app = new App("/app", ws);
    app.servePath("/static", "/test-resources");
    app.setRequireLoggedInByDefault(true);
    app.setLoginPage("/static/login.html");
    app.scan(this);
    app.start();

    SimpleClient client = new SimpleClient(ws);
    Assert.assertEquals("Please login", client.get("/app/test"));
  }

  @Test
  public void testServePathWithProtectedAccess() throws Exception {
    app = new App();
    app.servePath("/static", "/test-resources", null, true);
    app.setLoginPage("/static/login.html");
    app.start();

    SimpleClient client = new SimpleClient(app.getServer());
    Assert.assertEquals("Please login", client.get("/static/anything"));
  }

  @Test
  public void testServeRootWithProtectedAccess() throws Exception {
    app = new App();
    app.servePath("/", "/test-resources/", null, true);
    app.setLoginPage("/login.html");
    app.start();

    SimpleClient client = new SimpleClient(app.getServer());
    Assert.assertEquals("Please login", client.get("/static/anything"));
  }

  @Test
  public void testServeRootWithProtectedAccessAndClassLoader() throws Exception {
    app = new App();
    app.servePath("/", "/test-resources/", getClass().getClassLoader(), true);
    app.setLoginPage("/login.html");
    app.start();

    SimpleClient client = new SimpleClient(app.getServer());
    Assert.assertEquals("Please login", client.get("/static/anything"));
  }

}
