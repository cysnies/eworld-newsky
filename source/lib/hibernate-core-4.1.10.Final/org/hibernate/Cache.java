package org.hibernate;

import java.io.Serializable;

public interface Cache {
   boolean containsEntity(Class var1, Serializable var2);

   boolean containsEntity(String var1, Serializable var2);

   void evictEntity(Class var1, Serializable var2);

   void evictEntity(String var1, Serializable var2);

   void evictEntityRegion(Class var1);

   void evictEntityRegion(String var1);

   void evictEntityRegions();

   void evictNaturalIdRegion(Class var1);

   void evictNaturalIdRegion(String var1);

   void evictNaturalIdRegions();

   boolean containsCollection(String var1, Serializable var2);

   void evictCollection(String var1, Serializable var2);

   void evictCollectionRegion(String var1);

   void evictCollectionRegions();

   boolean containsQuery(String var1);

   void evictDefaultQueryRegion();

   void evictQueryRegion(String var1);

   void evictQueryRegions();
}
