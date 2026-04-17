package org.hibernate;

import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class Version {
   public static String getVersionString() {
      return "4.1.10.Final";
   }

   public static void logVersion() {
      ((CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Version.class.getName())).version(getVersionString());
   }

   public static void main(String[] args) {
      System.out.println("Hibernate Core {" + getVersionString() + "}");
   }
}
