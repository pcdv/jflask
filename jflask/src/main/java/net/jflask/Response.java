package net.jflask;

import java.io.OutputStream;

public interface Response {

  void addHeader(String header, String value);

  void setStatus(int status);

  OutputStream getOutputStream();

}
