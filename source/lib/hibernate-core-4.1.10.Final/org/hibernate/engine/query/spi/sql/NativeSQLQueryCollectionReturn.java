package org.hibernate.engine.query.spi.sql;

import java.util.Map;
import org.hibernate.LockMode;

public class NativeSQLQueryCollectionReturn extends NativeSQLQueryNonScalarReturn {
   private final String ownerEntityName;
   private final String ownerProperty;
   private final int hashCode;

   public NativeSQLQueryCollectionReturn(String alias, String ownerEntityName, String ownerProperty, Map propertyResults, LockMode lockMode) {
      super(alias, propertyResults, lockMode);
      this.ownerEntityName = ownerEntityName;
      this.ownerProperty = ownerProperty;
      this.hashCode = this.determineHashCode();
   }

   public String getOwnerEntityName() {
      return this.ownerEntityName;
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
            NativeSQLQueryCollectionReturn that = (NativeSQLQueryCollectionReturn)o;
            if (this.ownerEntityName != null) {
               if (!this.ownerEntityName.equals(that.ownerEntityName)) {
                  return false;
               }
            } else if (that.ownerEntityName != null) {
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
      result = 31 * result + (this.ownerEntityName != null ? this.ownerEntityName.hashCode() : 0);
      result = 31 * result + (this.ownerProperty != null ? this.ownerProperty.hashCode() : 0);
      return result;
   }
}
