package net.jflask.sun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

public class FileHandler extends AbstractResourceHandler {

  private final Path localPath;

  public FileHandler(ContentTypeProvider mime, String rootURI, File localFile) {
    super(mime, rootURI);
    this.localPath = localFile.toPath();
  }

  @Override
  protected InputStream openPath(String p) throws FileNotFoundException {
    if(p.startsWith("/"))
      p = p.substring(1);
    return new FileInputStream(localPath.resolve(p).toFile());
  }
}
