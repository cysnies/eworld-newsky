package com.mysql.jdbc;

import java.io.InputStream;
import java.sql.SQLException;

public interface Statement extends java.sql.Statement {
   void enableStreamingResults() throws SQLException;

   void disableStreamingResults() throws SQLException;

   void setLocalInfileInputStream(InputStream var1);

   InputStream getLocalInfileInputStream();

   void setPingTarget(PingTarget var1);
}
