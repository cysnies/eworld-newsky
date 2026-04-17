package org.hibernate.internal.util.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CollectionHelper {
   public static final int MINIMUM_INITIAL_CAPACITY = 16;
   public static final float LOAD_FACTOR = 0.75F;
   /** @deprecated */
   @Deprecated
   public static final List EMPTY_LIST;
   /** @deprecated */
   @Deprecated
   public static final Collection EMPTY_COLLECTION;
   /** @deprecated */
   @Deprecated
   public static final Map EMPTY_MAP;

   private CollectionHelper() {
   }

   public static Map mapOfSize(int size) {
      return new HashMap(determineProperSizing(size), 0.75F);
   }

   public static int determineProperSizing(Map original) {
      return determineProperSizing(original.size());
   }

   public static int determineProperSizing(Set original) {
      return determineProperSizing(original.size());
   }

   public static int determineProperSizing(int numberOfElements) {
      int actual = (int)((float)numberOfElements / 0.75F) + 1;
      return Math.max(actual, 16);
   }

   public static ConcurrentHashMap concurrentMap(int expectedNumberOfElements) {
      return concurrentMap(expectedNumberOfElements, 0.75F);
   }

   public static ConcurrentHashMap concurrentMap(int expectedNumberOfElements, float loadFactor) {
      int size = expectedNumberOfElements + 1 + (int)((float)expectedNumberOfElements * loadFactor);
      return new ConcurrentHashMap(size, loadFactor);
   }

   public static List arrayList(int anticipatedSize) {
      return new ArrayList(anticipatedSize);
   }

   public static boolean isEmpty(Collection collection) {
      return collection == null || collection.isEmpty();
   }

   public static boolean isEmpty(Map map) {
      return map == null || map.isEmpty();
   }

   public static boolean isNotEmpty(Collection collection) {
      return !isEmpty(collection);
   }

   public static boolean isNotEmpty(Map map) {
      return !isEmpty(map);
   }

   static {
      EMPTY_LIST = Collections.EMPTY_LIST;
      EMPTY_COLLECTION = Collections.EMPTY_LIST;
      EMPTY_MAP = Collections.EMPTY_MAP;
   }
}
