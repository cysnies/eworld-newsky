package org.hibernate.engine.query.spi.sql;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;

public abstract class NativeSQLQueryNonScalarReturn implements NativeSQLQueryReturn, Serializable {
   private final String alias;
   private final LockMode lockMode;
   private final Map propertyResults = new HashMap();
   private final int hashCode;

   protected NativeSQLQueryNonScalarReturn(String alias, Map propertyResults, LockMode lockMode) {
      this.alias = alias;
      if (alias == null) {
         throw new HibernateException("alias must be specified");
      } else {
         this.lockMode = lockMode;
         if (propertyResults != null) {
            this.propertyResults.putAll(propertyResults);
         }

         this.hashCode = this.determineHashCode();
      }
   }

   public String getAlias() {
      return this.alias;
   }

   public LockMode getLockMode() {
      return this.lockMode;
   }

   public Map getPropertyResultsMap() {
      return Collections.unmodifiableMap(this.propertyResults);
   }

   public int hashCode() {
      return this.hashCode;
   }

   private int determineHashCode() {
      int result = this.alias != null ? this.alias.hashCode() : 0;
      result = 31 * result + this.getClass().getName().hashCode();
      result = 31 * result + (this.lockMode != null ? this.lockMode.hashCode() : 0);
      result = 31 * result + (this.propertyResults != null ? this.propertyResults.hashCode() : 0);
      return result;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         NativeSQLQueryNonScalarReturn that = (NativeSQLQueryNonScalarReturn)o;
         if (this.alias != null) {
            if (!this.alias.equals(that.alias)) {
               return false;
            }
         } else if (that.alias != null) {
            return false;
         }

         if (this.lockMode != null) {
            if (!this.lockMode.equals(that.lockMode)) {
               return false;
            }
         } else if (that.lockMode != null) {
            return false;
         }

         if (this.propertyResults != null) {
            if (!this.propertyResults.equals(that.propertyResults)) {
               return false;
            }
         } else if (that.propertyResults != null) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }
}
