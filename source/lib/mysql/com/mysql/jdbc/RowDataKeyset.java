package com.mysql.jdbc;

import java.sql.SQLException;

class RowDataKeyset implements RowData {
   private ResultSetInternalMethods keyset;

   private void buildKeysetColumnsClause(Field[] originalQueryMetadata) throws SQLException {
      StringBuffer buf = new StringBuffer();

      for(int i = 0; i < originalQueryMetadata.length; ++i) {
         if (originalQueryMetadata[i].isPrimaryKey()) {
            if (buf.length() != 0) {
               buf.append(", ");
            }

            buf.append("`");
            buf.append(originalQueryMetadata[i].getDatabaseName());
            buf.append("`.`");
            buf.append(originalQueryMetadata[i].getOriginalTableName());
            buf.append("`.`");
            buf.append(originalQueryMetadata[i].getOriginalName());
            buf.append("`");
         }
      }

   }

   private String extractWhereClause(String sql) {
      String delims = "'`\"";
      String canonicalSql = StringUtils.stripComments(sql, delims, delims, true, false, true, true);
      int whereClausePos = StringUtils.indexOfIgnoreCaseRespectMarker(0, canonicalSql, " WHERE ", delims, delims, false);
      return whereClausePos == -1 ? "" : canonicalSql.substring(whereClausePos);
   }

   public void addRow(ResultSetRow row) throws SQLException {
   }

   public void afterLast() throws SQLException {
   }

   public void beforeFirst() throws SQLException {
   }

   public void beforeLast() throws SQLException {
   }

   public void close() throws SQLException {
      SQLException caughtWhileClosing = null;
      if (this.keyset != null) {
         try {
            this.keyset.close();
         } catch (SQLException sqlEx) {
            caughtWhileClosing = sqlEx;
         }

         this.keyset = null;
      }

      if (caughtWhileClosing != null) {
         throw caughtWhileClosing;
      }
   }

   public ResultSetRow getAt(int index) throws SQLException {
      return null;
   }

   public int getCurrentRowNumber() throws SQLException {
      return 0;
   }

   public ResultSetInternalMethods getOwner() {
      return null;
   }

   public boolean hasNext() throws SQLException {
      return false;
   }

   public boolean isAfterLast() throws SQLException {
      return false;
   }

   public boolean isBeforeFirst() throws SQLException {
      return false;
   }

   public boolean isDynamic() throws SQLException {
      return false;
   }

   public boolean isEmpty() throws SQLException {
      return false;
   }

   public boolean isFirst() throws SQLException {
      return false;
   }

   public boolean isLast() throws SQLException {
      return false;
   }

   public void moveRowRelative(int rows) throws SQLException {
   }

   public ResultSetRow next() throws SQLException {
      return null;
   }

   public void removeRow(int index) throws SQLException {
   }

   public void setCurrentRow(int rowNumber) throws SQLException {
   }

   public void setOwner(ResultSetImpl rs) {
   }

   public int size() throws SQLException {
      return 0;
   }

   public boolean wasEmpty() {
      return false;
   }

   public void setMetadata(Field[] metadata) {
   }
}
