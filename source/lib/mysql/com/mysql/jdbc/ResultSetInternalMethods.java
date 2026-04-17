package com.mysql.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface ResultSetInternalMethods extends ResultSet {
   ResultSetInternalMethods copy() throws SQLException;

   boolean reallyResult();

   Object getObjectStoredProc(int var1, int var2) throws SQLException;

   Object getObjectStoredProc(int var1, Map var2, int var3) throws SQLException;

   Object getObjectStoredProc(String var1, int var2) throws SQLException;

   Object getObjectStoredProc(String var1, Map var2, int var3) throws SQLException;

   String getServerInfo();

   long getUpdateCount();

   long getUpdateID();

   void realClose(boolean var1) throws SQLException;

   void setFirstCharOfQuery(char var1);

   void setOwningStatement(StatementImpl var1);

   char getFirstCharOfQuery();

   void clearNextResult();

   ResultSetInternalMethods getNextResultSet();

   void setStatementUsedForFetchingRows(PreparedStatement var1);

   void setWrapperStatement(java.sql.Statement var1);

   void buildIndexMapping() throws SQLException;

   void initializeWithMetadata() throws SQLException;

   void redefineFieldsForDBMD(Field[] var1);

   void populateCachedMetaData(CachedResultSetMetaData var1) throws SQLException;

   void initializeFromCachedMetaData(CachedResultSetMetaData var1);
}
