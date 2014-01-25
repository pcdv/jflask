package jbootweb;

import java.io.IOException;

import jbootweb.util.Options;
import jbootweb.util.http.WebServer;

public class Main {

  public static void main(String[] args) throws IOException {
    @SuppressWarnings("resource")
    WebServer ws = new WebServer(Options.PORT, null);
    ws.servePath("/", "app/");
    System.out.println("Listening on http://0.0.0.0:" + ws.getPort());
  }
}
