package org.hibernate.engine.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ColumnNameCache {
   public static final float LOAD_FACTOR = 0.75F;
   private final Map columnNameToIndexCache;

   public ColumnNameCache(int columnCount) {
      this.columnNameToIndexCache = new ConcurrentHashMap(columnCount + (int)((float)columnCount * 0.75F) + 1, 0.75F);
   }

   public int getIndexForColumnName(String columnName, ResultSet rs) throws SQLException {
      Integer cached = (Integer)this.columnNameToIndexCache.get(columnName);
      if (cached != null) {
         return cached;
      } else {
         int index = rs.findColumn(columnName);
         this.columnNameToIndexCache.put(columnName, index);
         return index;
      }
   }
}
