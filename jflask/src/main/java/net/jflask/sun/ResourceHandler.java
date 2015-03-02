package net.jflask.sun;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Serves files nested in a jar from classpath.
 * 
 * @author pcdv
 */
public class ResourceHandler extends AbstractResourceHandler {

  private final String localPath;

  public ResourceHandler(ContentTypeProvider mime,
                         String rootURI,
                         String localPath) {
    super(mime, rootURI);
    this.localPath = localPath;
  }

  @Override
  protected InputStream openPath(String p) throws FileNotFoundException {
    p = localPath + p;

    if (!p.startsWith("/"))
      p = "/" + p;

    InputStream in = getClass().getResourceAsStream(p);
    if (in == null)
      throw new FileNotFoundException(p);

    return in;
  }
}
