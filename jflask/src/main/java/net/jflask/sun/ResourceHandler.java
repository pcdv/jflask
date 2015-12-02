package net.jflask.sun;

import java.io.FileNotFoundException;
import java.io.InputStream;

import net.jflask.App;

/**
 * Serves files nested in a jar from classpath.
 *
 * @author pcdv
 */
public class ResourceHandler extends AbstractResourceHandler {

  private final String localPath;

  private final ClassLoader loader;

  public ResourceHandler(App app,
                         ContentTypeProvider mime,
                         String rootURI,
                         String localPath,
                         ClassLoader loader, boolean restricted) {
    super(app, mime, rootURI, restricted);
    if (!localPath.endsWith("/"))
      localPath += "/";
    this.localPath = localPath;
    this.loader = loader;
  }

  @Override
  protected InputStream openPath(String p) throws FileNotFoundException {
    p = localPath + p;

    if (!p.startsWith("/"))
      p = "/" + p;

    InputStream in = loader == null ? getClass().getResourceAsStream(p)
                                    : loader.getResourceAsStream(p);
    if (in == null)
      throw new FileNotFoundException(p);

    return in;
  }
}
