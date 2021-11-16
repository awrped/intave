package de.jpx3.intave.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public interface Resource {
  boolean available();
  long lastModified();
  void write(InputStream inputStream);
  InputStream read();
  void delete();

  default List<String> lines() {
    List<String> lines;
    try (InputStream inputStream = read()) {
      Scanner scanner = new Scanner(inputStream, "UTF-8");
      lines = new ArrayList<>();
      while (scanner.hasNext()) {
        lines.add(scanner.next());
      }
    } catch (IOException exception) {
      exception.printStackTrace();
      return Collections.emptyList();
    }
    return lines;
  }

  default String asString() {
    StringBuilder builder = new StringBuilder();
    try (InputStream inputStream = read()) {
      Scanner scanner = new Scanner(inputStream, "UTF-8");
      while (scanner.hasNext()) {
        builder.append(scanner.next());
      }
    } catch (IOException exception) {
      exception.printStackTrace();
      return "";
    }
    return builder.toString();
  }

  default Resource compressed() {
    return Resources.compressed(this);
  }

  default Resource encrypted() {
    return Resources.encrypted(this);
  }

  default Resource locked(File lockTarget) {
    return Resources.locked(lockTarget, this);
  }

  default Resource retryReads(int retries) {
    return Resources.retryRead(this, retries);
  }
}
