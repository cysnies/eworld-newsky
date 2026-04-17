package org.hibernate.dialect.pagination;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.engine.spi.RowSelection;

public class NoopLimitHandler extends AbstractLimitHandler {
   public NoopLimitHandler(String sql, RowSelection selection) {
      super(sql, selection);
   }

   public String getProcessedSql() {
      return this.sql;
   }

   public int bindLimitParametersAtStartOfQuery(PreparedStatement statement, int index) {
      return 0;
   }

   public int bindLimitParametersAtEndOfQuery(PreparedStatement statement, int index) {
      return 0;
   }

   public void setMaxRows(PreparedStatement statement) throws SQLException {
      if (LimitHelper.hasMaxRows(this.selection)) {
         statement.setMaxRows(this.selection.getMaxRows() + this.convertToFirstRowValue(LimitHelper.getFirstRow(this.selection)));
      }

   }
}
