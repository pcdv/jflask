package net.jflask.test;

import net.jflask.CustomResponse;
import net.jflask.LoginPage;
import net.jflask.LoginRequired;
import net.jflask.Route;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests basic authentication mechanisms.
 *
 * @author pcdv
 */
public class LoginTest extends AbstractAppTest {

  @LoginPage
  @Route("/login")
  public String loginPage() {
    return "Please login";
  }

  @Route("/logout")
  public CustomResponse logout() {
    app.destroySession();
    return app.redirect("/login");
  }

  @Route("/app")
  @LoginRequired
  public String appPage() {
    return "Welcome";
  }

  @Route(value = "/login", method = "POST")
  public CustomResponse login() {
    String login = app.getRequest().getForm("login");
    String pass = app.getRequest().getForm("password");

    if (login.equals("foo") && pass.equals("bar")) {
      app.createSession(login);
      return app.redirect("/app");
    }

    return app.redirect("/login");
  }

  @Override
  protected void preScan() {
    app.setPort(8181);
  }

  @Test
  public void testLogin() throws Exception {
    assertEquals("Please login", client.get("/app"));
    assertEquals("Please login", client.post("/login", "login=foo&password="));
    assertEquals("Welcome", client.post("/login", "login=foo&password=bar"));
    assertEquals("Please login", client.get("/logout"));
    assertEquals("Please login", client.get("/app"));
  }
}
