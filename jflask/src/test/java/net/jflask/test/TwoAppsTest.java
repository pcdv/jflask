package net.jflask.test;

import java.nio.file.Files;

import net.jflask.App;
import net.jflask.Route;
import net.jflask.sun.WebServer;
import net.jflask.test.util.SimpleClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;

/**
 * Tests a single WebServer shared by two separate apps.
 *
 * @author pcdv
 */
public class TwoAppsTest {

  private SimpleClient client;

  private WebServer ws;

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private App app1, app2;

  @Before
  public void setUp() throws Exception {
    ws = new WebServer(0, null);

    app1 = new App("/app1", ws) {
      @Route("/hello")
      public String hello() {
        return "Hello from app1";
      }
    };

    app2 = new App("/app2", ws) {
      @Route("/hello")
      public String hello() {
        return "Hello from app2";
      }
    };

    app1.start();
    app2.start();

    client = new SimpleClient("localhost", ws.getPort());
  }

  @After
  public void tearDown() throws Exception {
    ws.close();
  }

  @Test
  public void testMethodHandlers() throws Exception {
    assertEquals("Hello from app1", client.get("/app1/hello"));
    assertEquals("Hello from app2", client.get("/app2/hello"));
  }

  @Test
  public void testServeDir() throws Exception {
    Files.write(tmp.newFile("foo").toPath(), "Foobar".getBytes());
    app1.serveDir("/stuff", tmp.getRoot());
    assertEquals("Foobar", client.get("/app1/stuff/foo"));
  }
}
