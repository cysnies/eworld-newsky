package org.hibernate.engine.query.spi.sql;

import java.util.Map;
import org.hibernate.LockMode;

public class NativeSQLQueryRootReturn extends NativeSQLQueryNonScalarReturn {
   private final String returnEntityName;
   private final int hashCode;

   public NativeSQLQueryRootReturn(String alias, String entityName, LockMode lockMode) {
      this(alias, entityName, (Map)null, lockMode);
   }

   public NativeSQLQueryRootReturn(String alias, String entityName, Map propertyResults, LockMode lockMode) {
      super(alias, propertyResults, lockMode);
      this.returnEntityName = entityName;
      this.hashCode = this.determineHashCode();
   }

   public String getReturnEntityName() {
      return this.returnEntityName;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            NativeSQLQueryRootReturn that = (NativeSQLQueryRootReturn)o;
            if (this.returnEntityName != null) {
               if (!this.returnEntityName.equals(that.returnEntityName)) {
                  return false;
               }
            } else if (that.returnEntityName != null) {
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
      result = 31 * result + (this.returnEntityName != null ? this.returnEntityName.hashCode() : 0);
      return result;
   }
}
