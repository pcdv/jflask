package samples;

import net.jflask.App;
import net.jflask.Route;

public class WebApp extends App {

  @Route("/hello/:name")
  public String hello(String name) {
    return "Hello " + name;
  }

  public static void main(String[] args) throws Exception {
    WebApp app = new WebApp();

    // static resources are served from local file system or directly
    // from the webapp jar
    app.servePath("/", "app/");

    app.getServer().setPort(Integer.getInteger("port", 8080));
    app.start();
    System.out.println("Listening on http://0.0.0.0:" + app.getPort());
  }
}
