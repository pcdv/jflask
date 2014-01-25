package jbootweb.util;

public interface Options {

  boolean DEBUG = Boolean.getBoolean("debug");

  int PORT = Integer.getInteger("port", 8080);

}
