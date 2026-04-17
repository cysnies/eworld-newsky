package org.hibernate.dialect;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public interface ColumnAliasExtractor {
   ColumnAliasExtractor COLUMN_LABEL_EXTRACTOR = new ColumnAliasExtractor() {
      public String extractColumnAlias(ResultSetMetaData metaData, int position) throws SQLException {
         return metaData.getColumnLabel(position);
      }
   };
   ColumnAliasExtractor COLUMN_NAME_EXTRACTOR = new ColumnAliasExtractor() {
      public String extractColumnAlias(ResultSetMetaData metaData, int position) throws SQLException {
         return metaData.getColumnName(position);
      }
   };

   String extractColumnAlias(ResultSetMetaData var1, int var2) throws SQLException;
}
