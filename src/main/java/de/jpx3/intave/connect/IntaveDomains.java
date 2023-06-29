package de.jpx3.intave.connect;

import com.google.common.collect.Lists;
import de.jpx3.intave.IntaveControl;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.annotate.Native;
import de.jpx3.intave.resource.Resource;
import de.jpx3.intave.resource.Resources;

import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public final class IntaveDomains {
  private static final Resource BASE_DOMAIN_RESOURCE = Resources.cacheResourceChain("https://raw.githubusercontent.com/intave/domains/main/base", "bdomains", TimeUnit.DAYS.toMillis(1));
  private static final Resource SERVICE_DOMAIN_RESOURCE = Resources.cacheResourceChain("https://raw.githubusercontent.com/intave/domains/main/service", "sdomains", TimeUnit.DAYS.toMillis(1));

  private static List<String> baseDomains = Lists.newArrayList();
  private static List<String> serviceDomains = Lists.newArrayList();

  public static void setup() {
    baseDomains.addAll(BASE_DOMAIN_RESOURCE.readLines());
    serviceDomains.addAll(SERVICE_DOMAIN_RESOURCE.readLines());
    baseDomains.removeIf(String::isEmpty);
    serviceDomains.removeIf(String::isEmpty);

    sortDomains();
  }

  private static void sortDomains() {
    Map<String, Long> baseDomainsLatency = latencyMap(baseDomains);
    Map<String, Long> serviceDomainsLatency = latencyMap(serviceDomains);

    baseDomains.sort((o1, o2) -> {
      long latency1 = baseDomainsLatency.get(o1);
      long latency2 = baseDomainsLatency.get(o2);
      return Long.compare(latency1, latency2);
    });
    baseDomains = filterUnreachable(baseDomains, "intave.de");

    serviceDomains.sort((o1, o2) -> {
      long latency1 = serviceDomainsLatency.get(o1);
      long latency2 = serviceDomainsLatency.get(o2);
      return Long.compare(latency1, latency2);
    });
    serviceDomains = filterUnreachable(serviceDomains, "service.intave.de");
  }

  private static List<String> filterUnreachable(List<String> domains, String deadFallback) {
    // remove unreachable domains from top down, if one domain is reachable, stop
    for (int i = 0; i < domains.size(); i++) {
      String domain = domains.get(i);
      if (reachable(domain)) {
        return domains.subList(i, domains.size());
      }
    }
    return Lists.newArrayList(deadFallback);
  }

  private static boolean reachable(String domain) {
    String url = "https://" + domain + "/connection-test.php";
    try {
      URLConnection connection = new URL(url).openConnection();
      connection.setConnectTimeout(800);
      connection.setReadTimeout(800);
      connection.setRequestProperty("User-Agent", "Intave/" + IntavePlugin.version());
      connection.connect();
      Scanner scanner = new Scanner(connection.getInputStream());
      String response = scanner.nextLine();
      scanner.close();
      return response.contains("success");
    } catch (Exception e) {
      if (IntaveControl.DISABLE_LICENSE_CHECK) {
        System.out.println("Could not connect to " + domain + " (" + url + "): " + e.getMessage());
      }
      return false;
    }
  }

  private static Map<String, Long> latencyMap(List<String> domains) {
    Map<String, Long> latencyMap = new HashMap<>();
    for (String domain : domains) {
      long latency = latency(domain);
      latencyMap.put(domain, latency);
    }
    return latencyMap;
  }

  private static long latency(String domain) {
    try {
      long start = System.currentTimeMillis();
      boolean reachable = InetAddress.getByName(domain).isReachable(2000);
      long end = System.currentTimeMillis();
      return reachable ? end - start : Long.MAX_VALUE;
    } catch (Exception e) {
      return Long.MAX_VALUE;
    }
  }

  public static String primaryBaseDomain() {
    return baseDomains.isEmpty() ? "intave.de" : baseDomains.get(0);
  }

  public static List<String> baseDomains() {
    return baseDomains;
  }

  @Native
  public static String primaryServiceDomain() {
    return serviceDomains.isEmpty() ? "service.intave.de" : serviceDomains.get(0);
  }

  public static List<String> serviceDomains() {
    return serviceDomains;
  }

  private static void clearCaches() {
    BASE_DOMAIN_RESOURCE.delete();
    SERVICE_DOMAIN_RESOURCE.delete();
  }
}
