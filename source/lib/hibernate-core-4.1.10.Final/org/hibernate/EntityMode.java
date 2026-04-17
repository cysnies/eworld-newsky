package org.hibernate;

public enum EntityMode {
   POJO("pojo"),
   MAP("dynamic-map");

   private final String name;
   private static final String DYNAMIC_MAP_NAME = MAP.name.toUpperCase();

   private EntityMode(String name) {
      this.name = name;
   }

   public String toString() {
      return this.name;
   }

   public static EntityMode parse(String entityMode) {
      if (entityMode == null) {
         return POJO;
      } else {
         entityMode = entityMode.toUpperCase();
         return DYNAMIC_MAP_NAME.equals(entityMode) ? MAP : valueOf(entityMode);
      }
   }
}
