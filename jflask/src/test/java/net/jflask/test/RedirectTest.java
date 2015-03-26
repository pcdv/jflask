package net.jflask.test;

import net.jflask.Route;
import net.jflask.CustomResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Misc Redirect  tests.
 */
public class RedirectTest extends AbstractAppTest {

  @Route("/foo")
  public CustomResponse foo() {
    return app.redirect("/bar");
  }

  @Route("/bar")
  public String bar() {
    return "foobar";
  }

  @Test
  public void testRedirect() throws Exception {
    assertEquals("foobar", client.get("/foo"));
  }
}
