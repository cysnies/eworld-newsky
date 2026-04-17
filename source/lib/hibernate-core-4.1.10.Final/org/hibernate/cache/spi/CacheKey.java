package org.hibernate.cache.spi;

import java.io.Serializable;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.type.Type;

public class CacheKey implements Serializable {
   private final Serializable key;
   private final Type type;
   private final String entityOrRoleName;
   private final String tenantId;
   private final int hashCode;

   public CacheKey(Serializable id, Type type, String entityOrRoleName, String tenantId, SessionFactoryImplementor factory) {
      this.key = id;
      this.type = type;
      this.entityOrRoleName = entityOrRoleName;
      this.tenantId = tenantId;
      this.hashCode = this.calculateHashCode(type, factory);
   }

   private int calculateHashCode(Type type, SessionFactoryImplementor factory) {
      int result = type.getHashCode(this.key, factory);
      result = 31 * result + (this.tenantId != null ? this.tenantId.hashCode() : 0);
      return result;
   }

   public Serializable getKey() {
      return this.key;
   }

   public String getEntityOrRoleName() {
      return this.entityOrRoleName;
   }

   public String getTenantId() {
      return this.tenantId;
   }

   public boolean equals(Object other) {
      if (other == null) {
         return false;
      } else if (this == other) {
         return true;
      } else if (this.hashCode == other.hashCode() && other instanceof CacheKey) {
         CacheKey that = (CacheKey)other;
         return EqualsHelper.equals(this.entityOrRoleName, that.entityOrRoleName) && this.type.isEqual(this.key, that.key) && EqualsHelper.equals(this.tenantId, that.tenantId);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.hashCode;
   }

   public String toString() {
      return this.entityOrRoleName + '#' + this.key.toString();
   }
}
