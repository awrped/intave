package de.jpx3.intave.resource;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public final class FileSpreadLayer implements Resource {
  private final File targetFile;
  private final Resource targetResource;
  private final Function<File, Resource> resourcer;
  private final Resource[] spread;

  public FileSpreadLayer(File targetFile, Function<File, Resource> resourcer, int spreadAmount) {
    this.targetFile = targetFile;
    this.targetResource = resourcer.apply(targetFile);
    this.resourcer = resourcer;
    this.spread = new Resource[spreadAmount];
    if (targetResource.available()) {
      prepare();
    }
  }

  private boolean prepared = false;

  public synchronized void prepare() {
    if (prepared) {
      return;
    }
    prepared = true;
    for (int i = 0; i < spread.length; i++) {
      File copy = new File(targetFile + ".spr" + i);
      spread[i] = resourcer.apply(copy);
      boolean exists = copy.exists();
      boolean empty = copy.length() == 0;
      if (!exists || empty) {
        spread[i].write(targetResource.read());
      }
    }
  }

  @Override
  public boolean available() {
    return targetResource.available() && Arrays.stream(spread).anyMatch(Resource::available);
  }

  @Override
  public long lastModified() {
    return targetResource.lastModified();
  }

  @Override
  public void write(InputStream inputStream) {
    targetResource.write(inputStream);
    prepare();
    copyMainToSpread();
  }

  private void copyMainToSpread() {
    for (Resource resource : spread) {
      resource.write(targetResource.read());
    }
  }

  @Override
  public InputStream read() {
    prepare();
    while (available()) {
      int random = ThreadLocalRandom.current().nextInt(0, spread.length);
      Resource spreadResource = spread[random];
      if (spreadResource.available()) {
        return spreadResource.read();
      }
    }
    return targetResource.read();
  }

  @Override
  public void delete() {
    targetResource.delete();
    eraseSpread();
  }

  private void eraseSpread() {
    for (Resource resource : spread) {
      if (resource != null) {
        resource.delete();
      }
    }
  }
}
