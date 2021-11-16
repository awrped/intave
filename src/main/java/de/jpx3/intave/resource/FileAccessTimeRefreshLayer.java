package de.jpx3.intave.resource;

import java.io.File;
import java.io.InputStream;

public final class FileAccessTimeRefreshLayer implements Resource {
  private final Resource target;
  private final File targetFile;

  public FileAccessTimeRefreshLayer(Resource target, File targetFile) {
    this.target = target;
    this.targetFile = targetFile;
  }

  @Override
  public boolean available() {
    return target.available();
  }

  @Override
  public long lastModified() {
    return target.lastModified();
  }

  @Override
  public void write(InputStream inputStream) {
    target.write(inputStream);
  }

  @Override
  public InputStream read() {
    if (available()) {
      targetFile.setLastModified(System.currentTimeMillis());
    }
    return target.read();
  }

  @Override
  public void delete() {
    target.delete();
  }
}
