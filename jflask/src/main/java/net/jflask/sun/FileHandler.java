package net.jflask.sun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

import net.jflask.App;

public class FileHandler extends AbstractResourceHandler {

  private final Path localPath;

  public FileHandler(App app,
                     ContentTypeProvider mime,
                     String rootURI,
                     File localFile,
                     boolean restricted) {
    super(app, mime, rootURI, restricted);
    this.localPath = localFile.toPath();
  }

  @Override
  protected InputStream openPath(String p) throws FileNotFoundException {
    if (p.startsWith("/"))
      p = p.substring(1);
    return new FileInputStream(localPath.resolve(p).toFile());
  }
}
