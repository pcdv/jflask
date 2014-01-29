package jbootweb.util.http;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileHandler extends AbstractResourceHandler {

  public FileHandler(ContentTypeProvider mime, String rootURI, String localPath) {
    super(mime, rootURI, localPath);
  }

  @Override
  protected InputStream openPath(String p) throws FileNotFoundException {
    return new FileInputStream(p);
  }
}
