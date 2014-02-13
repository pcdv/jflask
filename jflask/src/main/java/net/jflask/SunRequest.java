package net.jflask;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

  public String getMethod() {
    return exch.getRequestMethod();
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

  public InputStream getInputStream() {
    return exch.getRequestBody();
  }

  // /////////// Response methods

  public void addHeader(String header, String value) {
    exch.getResponseHeaders().add(header, value);
  }

  public void setStatus(int status) {
    try {
      exch.sendResponseHeaders(status, 0);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public OutputStream getOutputStream() {
    return exch.getResponseBody();
  }

}
