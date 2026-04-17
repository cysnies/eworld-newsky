package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.List;

public class RowDataStatic implements RowData {
   private Field[] metadata;
   private int index = -1;
   ResultSetImpl owner;
   private List rows;

   public RowDataStatic(List rows) {
      this.rows = rows;
   }

   public void addRow(ResultSetRow row) {
      this.rows.add(row);
   }

   public void afterLast() {
      this.index = this.rows.size();
   }

   public void beforeFirst() {
      this.index = -1;
   }

   public void beforeLast() {
      this.index = this.rows.size() - 2;
   }

   public void close() {
   }

   public ResultSetRow getAt(int atIndex) throws SQLException {
      return atIndex >= 0 && atIndex < this.rows.size() ? ((ResultSetRow)this.rows.get(atIndex)).setMetadata(this.metadata) : null;
   }

   public int getCurrentRowNumber() {
      return this.index;
   }

   public ResultSetInternalMethods getOwner() {
      return this.owner;
   }

   public boolean hasNext() {
      boolean hasMore = this.index + 1 < this.rows.size();
      return hasMore;
   }

   public boolean isAfterLast() {
      return this.index >= this.rows.size();
   }

   public boolean isBeforeFirst() {
      return this.index == -1 && this.rows.size() != 0;
   }

   public boolean isDynamic() {
      return false;
   }

   public boolean isEmpty() {
      return this.rows.size() == 0;
   }

   public boolean isFirst() {
      return this.index == 0;
   }

   public boolean isLast() {
      if (this.rows.size() == 0) {
         return false;
      } else {
         return this.index == this.rows.size() - 1;
      }
   }

   public void moveRowRelative(int rowsToMove) {
      this.index += rowsToMove;
   }

   public ResultSetRow next() throws SQLException {
      ++this.index;
      if (this.index < this.rows.size()) {
         ResultSetRow row = (ResultSetRow)this.rows.get(this.index);
         return row.setMetadata(this.metadata);
      } else {
         return null;
      }
   }

   public void removeRow(int atIndex) {
      this.rows.remove(atIndex);
   }

   public void setCurrentRow(int newIndex) {
      this.index = newIndex;
   }

   public void setOwner(ResultSetImpl rs) {
      this.owner = rs;
   }

   public int size() {
      return this.rows.size();
   }

   public boolean wasEmpty() {
      return this.rows != null && this.rows.size() == 0;
   }

   public void setMetadata(Field[] metadata) {
      this.metadata = metadata;
   }
}
