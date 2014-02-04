package jbootweb.flask;

import com.sun.net.httpserver.HttpExchange;

import java.util.List;

public class SunRequest implements Request, Response {

  private final HttpExchange exch;

  private final int qsMark;

  private final String uri;

  public SunRequest(HttpExchange r) {
    this.exch = r;
    this.uri = r.getRequestURI().toString();
    this.qsMark = uri.indexOf('?');
  }

  public String getRequestURI() {
    return qsMark >= 0 ? uri.substring(0, qsMark) : uri;
  }

  public String getQueryString() {
    return qsMark >= 0 ? uri.substring(qsMark + 1) : null;
  }

  public String getArg(String name, String def) {
    if (qsMark == -1)
      return def;
    String qs = getQueryString();
    String[] tok = qs.split("&");
    for (String s : tok) {
      if (s.startsWith(name)) {
        if (s.length() > name.length() && s.charAt(name.length()) == '=')
          return s.substring(name.length() + 1);
      }
    }
    return def;
  }

  public List<String> getArgs(String name) {
    // TODO
    return null;
  }

  public void add(String header, String value) {
    exch.getResponseHeaders().add(header, value);
  }

}
