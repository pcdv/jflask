package net.jflask.test;

import java.io.IOException;

import net.jflask.App;
import org.junit.After;
import org.junit.Before;

public class AbstractAppTest {

  protected App app;

  protected SimpleClient client;

  @Before
  public void setUp() throws IOException {
    app = new App();
    app.setPort(0);

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
  }

}
