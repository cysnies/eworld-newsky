package org.hibernate.dialect.pagination;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.RowSelection;

public class LegacyLimitHandler extends AbstractLimitHandler {
   private final Dialect dialect;

   public LegacyLimitHandler(Dialect dialect, String sql, RowSelection selection) {
      super(sql, selection);
      this.dialect = dialect;
   }

   public boolean supportsLimit() {
      return this.dialect.supportsLimit();
   }

   public boolean supportsLimitOffset() {
      return this.dialect.supportsLimitOffset();
   }

   public boolean supportsVariableLimit() {
      return this.dialect.supportsVariableLimit();
   }

   public boolean bindLimitParametersInReverseOrder() {
      return this.dialect.bindLimitParametersInReverseOrder();
   }

   public boolean bindLimitParametersFirst() {
      return this.dialect.bindLimitParametersFirst();
   }

   public boolean useMaxForLimit() {
      return this.dialect.useMaxForLimit();
   }

   public boolean forceLimitUsage() {
      return this.dialect.forceLimitUsage();
   }

   public int convertToFirstRowValue(int zeroBasedFirstResult) {
      return this.dialect.convertToFirstRowValue(zeroBasedFirstResult);
   }

   public String getProcessedSql() {
      boolean useLimitOffset = this.supportsLimit() && this.supportsLimitOffset() && LimitHelper.hasFirstRow(this.selection) && LimitHelper.hasMaxRows(this.selection);
      return this.dialect.getLimitString(this.sql, useLimitOffset ? LimitHelper.getFirstRow(this.selection) : 0, this.getMaxOrLimit());
   }
}
