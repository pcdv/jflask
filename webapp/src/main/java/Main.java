

import java.io.IOException;

import jbootweb.flask.App;
import jbootweb.flask.Route;
import jbootweb.util.http.WebServer;

public class Main {

  /**
   * Old example.
  public static void main2(String[] args) throws IOException {
    @SuppressWarnings("resource")
    WebServer ws = new WebServer(Options.PORT, null);
    ws.servePath("/", "app/");
    System.out.println("Listening on http://0.0.0.0:" + ws.getPort());
  }
   */

  /**
   * New example using the Flask clone.
   */
  public static void main(String[] args) throws IOException {
    App app = new App() {
      @Route("/hello/:name")
      public String hello(String name) {
        return "Hello " + name;
      }
    };
    app.setPort(Integer.getInteger("port"));
    app.servePath("/", "app/");
    app.start();
    System.out.println("Listening on http://0.0.0.0:" + app.getPort());
  }
}
