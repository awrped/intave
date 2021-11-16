package de.jpx3.intave.resource;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.annotate.Native;
import de.jpx3.intave.security.ContextSecrets;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import static de.jpx3.intave.IntaveControl.GOMME_MODE;

public final class Resources {
  public static Resource resourceFromFile(File file) {
    return new FileResource(file);
  }

  public static Resource resourceFromWeb(URL url) {
    return new WebResource(url);
  }

  static Resource locked(File targetFile, Resource resource) {
    return new LockingLayer(targetFile, resource);
  }

  static Resource refreshFileAccessDateOnRead(File targetFile, Resource resource) {
    return new FileAccessTimeRefreshLayer(resource, targetFile);
  }

  static Resource encrypted(Resource resource) {
    return new EncryptionLayer(resource);
  }

  static Resource compressed(Resource resource) {
    // add later
    return resource;
  }

  static Resource retryRead(Resource resource, int retries) {
    return new RetryReadLayer(resource, retries);
  }

  static Resource fileSpread(File file, Function<File, Resource> resourcer, int spreads) {
    return new FileSpreadLayer(file, resourcer, spreads);
  }

  private final static int CLASS_VERSION = 4;

  @Native
  public static Resource versionDependentEncryptedFileResourceChain(String identifier) {
    File file = fileLocationOf(new UUID(~identifier.hashCode() | (CLASS_VERSION | CLASS_VERSION << 2), ~IntavePlugin.version().hashCode()) + "e");
    return refreshFileAccessDateOnRead(file, resourceFromFile(file).encrypted());
  }

  @Native
  public static Resource encryptedFileResourceChain(String identifier) {
    File file = fileLocationOf(new UUID(~identifier.hashCode() | (CLASS_VERSION | CLASS_VERSION << 2), -391180952) + "e");
    return refreshFileAccessDateOnRead(file, resourceFromFile(file).encrypted());
  }

  public static Resource cacheResourceChain(
    String url,
    String identifier,
    long expires
  ) {
    try {
      return cacheResourceChain(new URL(url), identifier, expires);
    } catch (MalformedURLException exception) {
      throw new IllegalStateException(exception);
    }
  }

  @Native
  public static Resource cacheResourceChain(
    URL url,
    String identifier,
    long expires
  ) {
    File file = fileLocationOf(new UUID(~identifier.hashCode(), ~IntavePlugin.version().hashCode()) + "f");
    Resource cache = fileSpread(file, theFile -> resourceFromFile(theFile).locked(theFile), 8).encrypted();
    Resource access = resourceFromWeb(url);
    return new ResourceCache(cache, access, expires).retryReads(3);
  }

  private static File fileLocationOf(String resourceId) {
    String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    File workDirectory;
    String filePath;
    if (operatingSystem.contains("win")) {
      filePath = System.getenv("APPDATA") + "/Intave/";
    } else {
      if (GOMME_MODE) {
        filePath = ContextSecrets.secret("cache-directory");
      } else {
        filePath = System.getProperty("user.home") + "/.intave/";
      }
    }
    workDirectory = new File(filePath);
    if (!workDirectory.exists()) {
      workDirectory.mkdir();
    }
    return new File(workDirectory, resourceId);
  }
}
