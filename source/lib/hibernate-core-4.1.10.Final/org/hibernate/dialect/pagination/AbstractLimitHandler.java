package org.hibernate.dialect.pagination;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.engine.spi.RowSelection;

public abstract class AbstractLimitHandler implements LimitHandler {
   protected final String sql;
   protected final RowSelection selection;

   public AbstractLimitHandler(String sql, RowSelection selection) {
      this.sql = sql;
      this.selection = selection;
   }

   public boolean supportsLimit() {
      return false;
   }

   public boolean supportsLimitOffset() {
      return this.supportsLimit();
   }

   public boolean supportsVariableLimit() {
      return this.supportsLimit();
   }

   public boolean bindLimitParametersInReverseOrder() {
      return false;
   }

   public boolean bindLimitParametersFirst() {
      return false;
   }

   public boolean useMaxForLimit() {
      return false;
   }

   public boolean forceLimitUsage() {
      return false;
   }

   public int convertToFirstRowValue(int zeroBasedFirstResult) {
      return zeroBasedFirstResult;
   }

   public String getProcessedSql() {
      throw new UnsupportedOperationException("Paged queries not supported by " + this.getClass().getName());
   }

   public int bindLimitParametersAtStartOfQuery(PreparedStatement statement, int index) throws SQLException {
      return this.bindLimitParametersFirst() ? this.bindLimitParameters(statement, index) : 0;
   }

   public int bindLimitParametersAtEndOfQuery(PreparedStatement statement, int index) throws SQLException {
      return !this.bindLimitParametersFirst() ? this.bindLimitParameters(statement, index) : 0;
   }

   public void setMaxRows(PreparedStatement statement) throws SQLException {
   }

   protected int bindLimitParameters(PreparedStatement statement, int index) throws SQLException {
      if (this.supportsVariableLimit() && LimitHelper.hasMaxRows(this.selection)) {
         int firstRow = this.convertToFirstRowValue(LimitHelper.getFirstRow(this.selection));
         int lastRow = this.getMaxOrLimit();
         boolean hasFirstRow = this.supportsLimitOffset() && (firstRow > 0 || this.forceLimitUsage());
         boolean reverse = this.bindLimitParametersInReverseOrder();
         if (hasFirstRow) {
            statement.setInt(index + (reverse ? 1 : 0), firstRow);
         }

         statement.setInt(index + (!reverse && hasFirstRow ? 1 : 0), lastRow);
         return hasFirstRow ? 2 : 1;
      } else {
         return 0;
      }
   }

   protected int getMaxOrLimit() {
      int firstRow = this.convertToFirstRowValue(LimitHelper.getFirstRow(this.selection));
      int lastRow = this.selection.getMaxRows();
      return this.useMaxForLimit() ? lastRow + firstRow : lastRow;
   }
}
