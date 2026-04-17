package org.hibernate.hql.internal;

import java.util.HashMap;
import java.util.Map;

public final class CollectionProperties {
   public static final Map HQL_COLLECTION_PROPERTIES = new HashMap();
   private static final String COLLECTION_INDEX_LOWER = "index".toLowerCase();

   private CollectionProperties() {
   }

   public static boolean isCollectionProperty(String name) {
      String key = name.toLowerCase();
      return COLLECTION_INDEX_LOWER.equals(key) ? false : HQL_COLLECTION_PROPERTIES.containsKey(key);
   }

   public static String getNormalizedPropertyName(String name) {
      return (String)HQL_COLLECTION_PROPERTIES.get(name);
   }

   public static boolean isAnyCollectionProperty(String name) {
      String key = name.toLowerCase();
      return HQL_COLLECTION_PROPERTIES.containsKey(key);
   }

   static {
      HQL_COLLECTION_PROPERTIES.put("elements".toLowerCase(), "elements");
      HQL_COLLECTION_PROPERTIES.put("indices".toLowerCase(), "indices");
      HQL_COLLECTION_PROPERTIES.put("size".toLowerCase(), "size");
      HQL_COLLECTION_PROPERTIES.put("maxIndex".toLowerCase(), "maxIndex");
      HQL_COLLECTION_PROPERTIES.put("minIndex".toLowerCase(), "minIndex");
      HQL_COLLECTION_PROPERTIES.put("maxElement".toLowerCase(), "maxElement");
      HQL_COLLECTION_PROPERTIES.put("minElement".toLowerCase(), "minElement");
      HQL_COLLECTION_PROPERTIES.put(COLLECTION_INDEX_LOWER, "index");
   }
}
