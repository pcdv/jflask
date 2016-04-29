package net.jflask.test;

import java.io.IOException;

import net.jflask.App;
import net.jflask.sun.WebServer;
import net.jflask.test.util.SimpleClient;
import net.jflask.test.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

public class AbstractAppTest {

  protected App app;

  protected SimpleClient client;

  /**
   * Checks that the test did not leave behind any running thread.
   */
  @Rule
  public ThreadState.ThreadStateRule noZombies =
      new ThreadState.ThreadStateRule();

  @Before
  public void setUp() throws IOException {
    WebServer ws = new WebServer(0, null);
    app = new App(ws);

    preScan();
    app.scan(this);

    preStart();
    app.start();

    client = new SimpleClient("localhost", app.getPort());
  }

  /**
   * Override this method to execute code before the app scans request
   * handlers.
   */
  protected void preScan() {
  }

  /**
   * Override this method to execute code before the app is started.
   */
  protected void preStart() {
  }

  @After
  public void tearDown() {
    app.destroy();
    app.getServer().close();
  }

}
