package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.HibernateException;

public interface LoadEventListener extends Serializable {
   LoadType RELOAD = (new LoadType("GET")).setAllowNulls(false).setAllowProxyCreation(false).setCheckDeleted(true).setNakedEntityReturned(false);
   LoadType GET = (new LoadType("GET")).setAllowNulls(true).setAllowProxyCreation(false).setCheckDeleted(true).setNakedEntityReturned(false);
   LoadType LOAD = (new LoadType("LOAD")).setAllowNulls(false).setAllowProxyCreation(true).setCheckDeleted(true).setNakedEntityReturned(false);
   LoadType IMMEDIATE_LOAD = (new LoadType("IMMEDIATE_LOAD")).setAllowNulls(true).setAllowProxyCreation(false).setCheckDeleted(false).setNakedEntityReturned(true);
   LoadType INTERNAL_LOAD_EAGER = (new LoadType("INTERNAL_LOAD_EAGER")).setAllowNulls(false).setAllowProxyCreation(false).setCheckDeleted(false).setNakedEntityReturned(false);
   LoadType INTERNAL_LOAD_LAZY = (new LoadType("INTERNAL_LOAD_LAZY")).setAllowNulls(false).setAllowProxyCreation(true).setCheckDeleted(false).setNakedEntityReturned(false);
   LoadType INTERNAL_LOAD_NULLABLE = (new LoadType("INTERNAL_LOAD_NULLABLE")).setAllowNulls(true).setAllowProxyCreation(false).setCheckDeleted(false).setNakedEntityReturned(false);

   void onLoad(LoadEvent var1, LoadType var2) throws HibernateException;

   public static final class LoadType {
      private String name;
      private boolean nakedEntityReturned;
      private boolean allowNulls;
      private boolean checkDeleted;
      private boolean allowProxyCreation;

      private LoadType(String name) {
         this.name = name;
      }

      public boolean isAllowNulls() {
         return this.allowNulls;
      }

      private LoadType setAllowNulls(boolean allowNulls) {
         this.allowNulls = allowNulls;
         return this;
      }

      public boolean isNakedEntityReturned() {
         return this.nakedEntityReturned;
      }

      private LoadType setNakedEntityReturned(boolean immediateLoad) {
         this.nakedEntityReturned = immediateLoad;
         return this;
      }

      public boolean isCheckDeleted() {
         return this.checkDeleted;
      }

      private LoadType setCheckDeleted(boolean checkDeleted) {
         this.checkDeleted = checkDeleted;
         return this;
      }

      public boolean isAllowProxyCreation() {
         return this.allowProxyCreation;
      }

      private LoadType setAllowProxyCreation(boolean allowProxyCreation) {
         this.allowProxyCreation = allowProxyCreation;
         return this;
      }

      public String getName() {
         return this.name;
      }

      public String toString() {
         return this.name;
      }
   }
}
