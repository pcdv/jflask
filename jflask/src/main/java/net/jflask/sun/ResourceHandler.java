package net.jflask.sun;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Serves files nested in a jar from classpath.
 *
 * @author pcdv
 */
public class ResourceHandler extends AbstractResourceHandler {

  public ResourceHandler(ContentTypeProvider mime, String rootURI, String localPath) {
    super(mime, rootURI, localPath);
  }

  @Override
  protected InputStream openPath(String p) throws FileNotFoundException {
    if (!p.startsWith("/"))
      p = "/" + p;

    InputStream in = String.class.getResourceAsStream(p);
    if (in == null)
      throw new FileNotFoundException(p);
    return in;
  }
}
