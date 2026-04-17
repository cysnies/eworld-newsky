package com.mysql.jdbc;

import java.sql.SQLException;

public interface RowData {
   int RESULT_SET_SIZE_UNKNOWN = -1;

   void addRow(ResultSetRow var1) throws SQLException;

   void afterLast() throws SQLException;

   void beforeFirst() throws SQLException;

   void beforeLast() throws SQLException;

   void close() throws SQLException;

   ResultSetRow getAt(int var1) throws SQLException;

   int getCurrentRowNumber() throws SQLException;

   ResultSetInternalMethods getOwner();

   boolean hasNext() throws SQLException;

   boolean isAfterLast() throws SQLException;

   boolean isBeforeFirst() throws SQLException;

   boolean isDynamic() throws SQLException;

   boolean isEmpty() throws SQLException;

   boolean isFirst() throws SQLException;

   boolean isLast() throws SQLException;

   void moveRowRelative(int var1) throws SQLException;

   ResultSetRow next() throws SQLException;

   void removeRow(int var1) throws SQLException;

   void setCurrentRow(int var1) throws SQLException;

   void setOwner(ResultSetImpl var1);

   int size() throws SQLException;

   boolean wasEmpty();

   void setMetadata(Field[] var1);
}
