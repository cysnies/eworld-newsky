package org.hibernate.metamodel.binding;

import org.hibernate.cache.spi.access.AccessType;

public class Caching {
   private String region;
   private AccessType accessType;
   private boolean cacheLazyProperties;

   public Caching() {
   }

   public Caching(String region, AccessType accessType, boolean cacheLazyProperties) {
      this.region = region;
      this.accessType = accessType;
      this.cacheLazyProperties = cacheLazyProperties;
   }

   public String getRegion() {
      return this.region;
   }

   public void setRegion(String region) {
      this.region = region;
   }

   public AccessType getAccessType() {
      return this.accessType;
   }

   public void setAccessType(AccessType accessType) {
      this.accessType = accessType;
   }

   public boolean isCacheLazyProperties() {
      return this.cacheLazyProperties;
   }

   public void setCacheLazyProperties(boolean cacheLazyProperties) {
      this.cacheLazyProperties = cacheLazyProperties;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Caching");
      sb.append("{region='").append(this.region).append('\'');
      sb.append(", accessType=").append(this.accessType);
      sb.append(", cacheLazyProperties=").append(this.cacheLazyProperties);
      sb.append('}');
      return sb.toString();
   }
}
