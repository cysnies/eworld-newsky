package org.hibernate.engine.query.spi.sql;

import java.util.Map;
import org.hibernate.LockMode;

public class NativeSQLQueryJoinReturn extends NativeSQLQueryNonScalarReturn {
   private final String ownerAlias;
   private final String ownerProperty;
   private final int hashCode;

   public NativeSQLQueryJoinReturn(String alias, String ownerAlias, String ownerProperty, Map propertyResults, LockMode lockMode) {
      super(alias, propertyResults, lockMode);
      this.ownerAlias = ownerAlias;
      this.ownerProperty = ownerProperty;
      this.hashCode = this.determineHashCode();
   }

   public String getOwnerAlias() {
      return this.ownerAlias;
   }

   public String getOwnerProperty() {
      return this.ownerProperty;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            NativeSQLQueryJoinReturn that = (NativeSQLQueryJoinReturn)o;
            if (this.ownerAlias != null) {
               if (!this.ownerAlias.equals(that.ownerAlias)) {
                  return false;
               }
            } else if (that.ownerAlias != null) {
               return false;
            }

            if (this.ownerProperty != null) {
               if (!this.ownerProperty.equals(that.ownerProperty)) {
                  return false;
               }
            } else if (that.ownerProperty != null) {
               return false;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.hashCode;
   }

   private int determineHashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.ownerAlias != null ? this.ownerAlias.hashCode() : 0);
      result = 31 * result + (this.ownerProperty != null ? this.ownerProperty.hashCode() : 0);
      return result;
   }
}
