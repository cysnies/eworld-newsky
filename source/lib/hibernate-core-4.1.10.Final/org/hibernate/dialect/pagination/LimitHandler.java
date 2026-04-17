package org.hibernate.dialect.pagination;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface LimitHandler {
   boolean supportsLimit();

   boolean supportsLimitOffset();

   String getProcessedSql();

   int bindLimitParametersAtStartOfQuery(PreparedStatement var1, int var2) throws SQLException;

   int bindLimitParametersAtEndOfQuery(PreparedStatement var1, int var2) throws SQLException;

   void setMaxRows(PreparedStatement var1) throws SQLException;
}
