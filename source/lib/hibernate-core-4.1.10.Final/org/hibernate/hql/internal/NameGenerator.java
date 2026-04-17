package org.hibernate.hql.internal;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

public final class NameGenerator {
   private NameGenerator() {
   }

   public static String[][] generateColumnNames(Type[] types, SessionFactoryImplementor f) throws MappingException {
      String[][] columnNames = new String[types.length][];

      for(int i = 0; i < types.length; ++i) {
         int span = types[i].getColumnSpan(f);
         columnNames[i] = new String[span];

         for(int j = 0; j < span; ++j) {
            columnNames[i][j] = scalarName(i, j);
         }
      }

      return columnNames;
   }

   public static String scalarName(int x, int y) {
      return "col_" + x + '_' + y + '_';
   }
}
