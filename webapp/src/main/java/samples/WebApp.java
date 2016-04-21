package samples;

import net.jflask.App;
import net.jflask.Route;

public class WebApp {

  public static void main(String[] args) throws Exception {
    App app = new App(Integer.getInteger("port", 8080));

    // NB: it is cleaner to define route handler in dedicated classes
    app.scan(new Object() {
      @Route("/hello/:name")
      public String hello(String name) {
        return "Hello " + name;
      }
    });

    // static resources are served from local file system or directly
    // from the web-app jar
    app.servePath("/", "app/");

    app.start();
    System.out.println("Listening on http://0.0.0.0:" + app.getPort());
  }
}
