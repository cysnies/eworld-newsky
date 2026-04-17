package com.mysql.jdbc.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache extends LinkedHashMap {
   private static final long serialVersionUID = 1L;
   protected int maxElements;

   public LRUCache(int maxSize) {
      super(maxSize);
      this.maxElements = maxSize;
   }

   protected boolean removeEldestEntry(Map.Entry eldest) {
      return this.size() > this.maxElements;
   }
}
