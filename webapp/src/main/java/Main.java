import java.io.IOException;

import net.jflask.App;
import net.jflask.Route;
import net.jflask.sun.WebServer;

public class Main {

  /**
   * New example using the Flask clone.
   */
  public static void main(String[] args) throws IOException {
    WebServer srv = new WebServer(Integer.getInteger("port", 8080), null);
    App app = new App(srv) {
      @Route("/hello/:name")
      public String hello(String name) {
        return "Hello " + name;
      }
    };
    app.servePath("/", "app/");
    app.start();
    System.out.println("Listening on http://0.0.0.0:" + app.getPort());
  }
}
