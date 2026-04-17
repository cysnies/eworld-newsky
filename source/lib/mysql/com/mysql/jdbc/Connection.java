package com.mysql.jdbc;

import com.mysql.jdbc.log.Log;
import java.sql.SQLException;
import java.util.TimeZone;

public interface Connection extends java.sql.Connection, ConnectionProperties {
   void changeUser(String var1, String var2) throws SQLException;

   void clearHasTriedMaster();

   java.sql.PreparedStatement clientPrepareStatement(String var1) throws SQLException;

   java.sql.PreparedStatement clientPrepareStatement(String var1, int var2) throws SQLException;

   java.sql.PreparedStatement clientPrepareStatement(String var1, int var2, int var3) throws SQLException;

   java.sql.PreparedStatement clientPrepareStatement(String var1, int[] var2) throws SQLException;

   java.sql.PreparedStatement clientPrepareStatement(String var1, int var2, int var3, int var4) throws SQLException;

   java.sql.PreparedStatement clientPrepareStatement(String var1, String[] var2) throws SQLException;

   int getActiveStatementCount();

   long getIdleFor();

   Log getLog() throws SQLException;

   String getServerCharacterEncoding();

   TimeZone getServerTimezoneTZ();

   String getStatementComment();

   boolean hasTriedMaster();

   boolean isInGlobalTx();

   void setInGlobalTx(boolean var1);

   boolean isMasterConnection();

   boolean isNoBackslashEscapesSet();

   boolean isSameResource(Connection var1);

   boolean lowerCaseTableNames();

   boolean parserKnowsUnicode();

   void ping() throws SQLException;

   void resetServerState() throws SQLException;

   java.sql.PreparedStatement serverPrepareStatement(String var1) throws SQLException;

   java.sql.PreparedStatement serverPrepareStatement(String var1, int var2) throws SQLException;

   java.sql.PreparedStatement serverPrepareStatement(String var1, int var2, int var3) throws SQLException;

   java.sql.PreparedStatement serverPrepareStatement(String var1, int var2, int var3, int var4) throws SQLException;

   java.sql.PreparedStatement serverPrepareStatement(String var1, int[] var2) throws SQLException;

   java.sql.PreparedStatement serverPrepareStatement(String var1, String[] var2) throws SQLException;

   void setFailedOver(boolean var1);

   void setPreferSlaveDuringFailover(boolean var1);

   void setStatementComment(String var1);

   void shutdownServer() throws SQLException;

   boolean supportsIsolationLevel();

   boolean supportsQuotedIdentifiers();

   boolean supportsTransactions();

   boolean versionMeetsMinimum(int var1, int var2, int var3) throws SQLException;

   void reportQueryTime(long var1);

   boolean isAbonormallyLongQuery(long var1);

   void initializeExtension(Extension var1) throws SQLException;
}
