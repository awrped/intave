package de.jpx3.intave.security;

import static de.jpx3.intave.library.asm.ClassVisitor.LICENSE_NAME;

public final class LicenseAccess {
  public static String network() {
    return LICENSE_NAME;//System.getProperty("java.net.serviceprovider.key");
  }

  public static String rawLicense() {
    return "dW9b4SrAMxc5hSfbbp9xawEOrXV47DpHezU5nM8Dfbx2nON72AzA2PFEbfSh6HChYeqKvRAVqMnkMUm36AKKWtPcz706drbT57stIIIIIMTQwYWI3YmMyMzU0NTRmM2M";
  }
}
