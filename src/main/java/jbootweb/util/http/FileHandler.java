package jbootweb.util.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileHandler extends AbstractResourceHandler {

  public FileHandler(WebServer srv, String rootURI, String localPath) {
    super(srv, rootURI, localPath);
  }

  @Override
  protected InputStream openPath(String p) throws FileNotFoundException {
    return new FileInputStream(p);
  }
}
