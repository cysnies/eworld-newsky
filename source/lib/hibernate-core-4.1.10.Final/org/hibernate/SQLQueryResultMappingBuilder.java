package org.hibernate;

import org.hibernate.engine.query.spi.sql.NativeSQLQueryReturn;
import org.hibernate.type.Type;

public interface SQLQueryResultMappingBuilder {
   public static class ScalarReturn {
      private final ReturnsHolder returnsHolder;
      private String name;
      private Type type;

      public ScalarReturn(ReturnsHolder returnsHolder) {
         this.returnsHolder = returnsHolder;
      }
   }

   public interface ReturnsHolder {
      void add(NativeSQLQueryReturn var1);
   }
}
