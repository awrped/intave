package de.jpx3.intave.version;

import de.jpx3.intave.resource.CompilerStreamFunctionProvider;

import java.util.ArrayList;
import java.util.List;

public final class ProtocolVersionRangesCompiler implements CompilerStreamFunctionProvider<ProtocolVersionRanges> {
  @Override
  public ProtocolVersionRanges apply(List<String> lines) {
    int lastEnd = Integer.MIN_VALUE;
    List<ProtocolVersionRange> ranges = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i).trim();
      if (line.startsWith("#")) {
        continue;
      }
      if (line.startsWith("up to")) {
        // format is "up to <number> is <version>"
        String[] split = line.split(" ");
        if (split.length != 5) {
          System.out.println("Invalid line format: " + line + " at line " + i);
          Thread.dumpStack();
          continue;
        }
        int protocolVersion = Integer.parseInt(split[2]);
        String version = split[4];
        ranges.add(new ProtocolVersionRange(lastEnd + 1, protocolVersion, version));
        lastEnd = protocolVersion;
      } else {
        // format is "<number> is <version>"
        String[] split = line.split(" is ");
        if (split.length != 2) {
          System.out.println("Invalid line format: " + line + " at line " + i);
          Thread.dumpStack();
          continue;
        }
        int protocolVersion = Integer.parseInt(split[0]);
        String version = split[1];
        if (protocolVersion <= lastEnd) {
          System.out.println("Invalid line format: " + line + " at line " + i);
          Thread.dumpStack();
          continue;
        }
        ranges.add(new ProtocolVersionRange(protocolVersion, protocolVersion, version));
        lastEnd = protocolVersion;
      }
    }
    return new ProtocolVersionRanges(ranges);
  }
}
