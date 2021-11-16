package de.jpx3.intave.resource;

import de.jpx3.intave.IntavePlugin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public final class WebResource implements Resource {
  private final URL url;

  public WebResource(URL url) {
    this.url = url;
  }

  @Override
  public boolean available() {
    return true;
  }

  @Override
  public long lastModified() {
    return System.currentTimeMillis();
  }

  @Override
  public void write(InputStream inputStream) {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream read() {
    try {
      URLConnection connection = url.openConnection();
      connection.addRequestProperty("User-Agent", "Intave/" + IntavePlugin.version());
      connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
      connection.addRequestProperty("Pragma", "no-cache");
      connection.setConnectTimeout(3000);
      connection.setReadTimeout(1000);
      return connection.getInputStream();
    } catch (Exception exception) {
      return new ByteArrayInputStream(new byte[0]);
    }
  }

  @Override
  public void delete() {
    throw new UnsupportedOperationException();
  }
}
