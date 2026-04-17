package com.mysql.jdbc;

import java.sql.SQLException;
import java.sql.Savepoint;

public interface ConnectionLifecycleInterceptor extends Extension {
   void close() throws SQLException;

   boolean commit() throws SQLException;

   boolean rollback() throws SQLException;

   boolean rollback(Savepoint var1) throws SQLException;

   boolean setAutoCommit(boolean var1) throws SQLException;

   boolean setCatalog(String var1) throws SQLException;

   boolean transactionBegun() throws SQLException;

   boolean transactionCompleted() throws SQLException;
}
