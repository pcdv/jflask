JFlask
======

JFlask is a micro web framework for Java inspired by [Flask](http://flask.pocoo.org/).




Here is an example of what a small JFlask web app looks like.
```java
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

    app.setPort(Integer.getInteger("port", 8080));
    app.start();
    System.out.println("Listening on http://0.0.0.0:" + app.getPort());
  }
}
```

Features
--------
 - Minimal size (around 20kB)
 - No external dependencies (it uses [HTTP server](http://docs.oracle.com/javase/7/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html) embedded in the JRE)
 - Flask-like request routing (with method annotations)
 - Easy standalone jar generation (executable with`java -jar mywebapp.jar`)


----------


