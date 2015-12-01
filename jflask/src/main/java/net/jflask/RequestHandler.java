package net.jflask;

import com.sun.net.httpserver.HttpHandler;

/**
 * @author pcdv
 */
public interface RequestHandler {
  App getApp();

  HttpHandler asHttpHandler();
}
