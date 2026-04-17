package org.hibernate.annotations.common;

import org.hibernate.annotations.common.util.impl.Log;
import org.hibernate.annotations.common.util.impl.LoggerFactory;

public class Version {
   private static final Log log = LoggerFactory.make(Version.class.getName());

   public static String getVersionString() {
      return "4.0.1.Final";
   }

   public static void touch() {
   }

   public static void main(String[] args) {
      System.out.println("Hibernate Commons Annotations {" + getVersionString() + "}");
   }

   static {
      log.version(getVersionString());
   }
}
