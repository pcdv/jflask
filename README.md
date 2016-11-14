JFlask
======

JFlask is a micro web framework for Java inspired by [Flask](http://flask.pocoo.org/).




Here is an example of what a small JFlask web app looks like.
```java
package samples;

import net.jflask.App;
import net.jflask.Route;

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
```

For more examples, look at the [junits](https://github.com/pcdv/jflask/tree/master/jflask/src/test/java/net/jflask/test)

Features
--------
 - Minimal size (around 28kB)
 - No external dependencies (it uses [HTTP server](http://docs.oracle.com/javase/7/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html) embedded in the JRE)
 - Flask-like request routing (with method annotations)
 - Easy standalone jar generation (executable with`java -jar mywebapp.jar`)

Requirements
------------
 - JRE 7 (or later)

Installation
------------
JFlask is available on jcenter.
```
  compile "net.jflask:jflask:0.28"
```

