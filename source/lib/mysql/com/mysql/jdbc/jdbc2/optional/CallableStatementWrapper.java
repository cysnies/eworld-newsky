package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Util;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public class CallableStatementWrapper extends PreparedStatementWrapper implements CallableStatement {
   private static final Constructor JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$jdbc2$optional$ConnectionWrapper;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$jdbc2$optional$MysqlPooledConnection;
   // $FF: synthetic field
   static Class class$java$sql$CallableStatement;

   protected static CallableStatementWrapper getInstance(ConnectionWrapper c, MysqlPooledConnection conn, CallableStatement toWrap) throws SQLException {
      return !Util.isJdbc4() ? new CallableStatementWrapper(c, conn, toWrap) : (CallableStatementWrapper)Util.handleNewInstance(JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR, new Object[]{c, conn, toWrap});
   }

   public CallableStatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, CallableStatement toWrap) {
      super(c, conn, toWrap);
   }

   public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterIndex, sqlType);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterIndex, sqlType, scale);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public boolean wasNull() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).wasNull();
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public String getString(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getString(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public boolean getBoolean(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getBoolean(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public byte getByte(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getByte(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public short getShort(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getShort(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public int getInt(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getInt(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public long getLong(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getLong(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0L;
      }
   }

   public float getFloat(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getFloat(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0.0F;
      }
   }

   public double getDouble(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getDouble(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return (double)0.0F;
      }
   }

   public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getBigDecimal(parameterIndex, scale);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public byte[] getBytes(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getBytes(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Date getDate(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getDate(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Time getTime(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getTime(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Timestamp getTimestamp(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Object getObject(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getObject(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getBigDecimal(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Object getObject(int parameterIndex, Map typeMap) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getObject(parameterIndex, typeMap);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Ref getRef(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getRef(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Blob getBlob(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getBlob(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Clob getClob(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getClob(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Array getArray(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getArray(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getDate(parameterIndex, cal);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getTime(parameterIndex, cal);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterIndex, cal);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).registerOutParameter(paramIndex, sqlType, typeName);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterName, sqlType);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterName, sqlType, scale);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterName, sqlType, typeName);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public URL getURL(int parameterIndex) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getURL(parameterIndex);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public void setURL(String parameterName, URL val) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setURL(parameterName, val);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setNull(String parameterName, int sqlType) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setNull(parameterName, sqlType);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setBoolean(String parameterName, boolean x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setBoolean(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setByte(String parameterName, byte x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setByte(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setShort(String parameterName, short x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setShort(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setInt(String parameterName, int x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setInt(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setLong(String parameterName, long x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setLong(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setFloat(String parameterName, float x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setFloat(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setDouble(String parameterName, double x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setDouble(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setBigDecimal(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setString(String parameterName, String x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setString(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setBytes(String parameterName, byte[] x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setBytes(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setDate(String parameterName, Date x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setDate(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setTime(String parameterName, Time x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setTime(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setTimestamp(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setAsciiStream(parameterName, x, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setBinaryStream(parameterName, x, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setObject(parameterName, x, targetSqlType, scale);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setObject(parameterName, x, targetSqlType);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setObject(String parameterName, Object x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setObject(parameterName, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setCharacterStream(parameterName, reader, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setDate(parameterName, x, cal);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setTime(parameterName, x, cal);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setTimestamp(parameterName, x, cal);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((CallableStatement)this.wrappedStmt).setNull(parameterName, sqlType, typeName);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public String getString(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getString(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public boolean getBoolean(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getBoolean(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public byte getByte(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getByte(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public short getShort(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getShort(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public int getInt(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getInt(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public long getLong(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getLong(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0L;
      }
   }

   public float getFloat(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getFloat(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0.0F;
      }
   }

   public double getDouble(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getDouble(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return (double)0.0F;
      }
   }

   public byte[] getBytes(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getBytes(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Date getDate(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getDate(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Time getTime(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getTime(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Timestamp getTimestamp(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Object getObject(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getObject(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public BigDecimal getBigDecimal(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getBigDecimal(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Object getObject(String parameterName, Map typeMap) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getObject(parameterName, typeMap);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Ref getRef(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getRef(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Blob getBlob(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getBlob(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Clob getClob(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getClob(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Array getArray(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getArray(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Date getDate(String parameterName, Calendar cal) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getDate(parameterName, cal);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Time getTime(String parameterName, Calendar cal) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getTime(parameterName, cal);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterName, cal);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public URL getURL(String parameterName) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return ((CallableStatement)this.wrappedStmt).getURL(parameterName);
         } else {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      if (Util.isJdbc4()) {
         try {
            JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4CallableStatementWrapper").getConstructor(class$com$mysql$jdbc$jdbc2$optional$ConnectionWrapper == null ? (class$com$mysql$jdbc$jdbc2$optional$ConnectionWrapper = class$("com.mysql.jdbc.jdbc2.optional.ConnectionWrapper")) : class$com$mysql$jdbc$jdbc2$optional$ConnectionWrapper, class$com$mysql$jdbc$jdbc2$optional$MysqlPooledConnection == null ? (class$com$mysql$jdbc$jdbc2$optional$MysqlPooledConnection = class$("com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection")) : class$com$mysql$jdbc$jdbc2$optional$MysqlPooledConnection, class$java$sql$CallableStatement == null ? (class$java$sql$CallableStatement = class$("java.sql.CallableStatement")) : class$java$sql$CallableStatement);
         } catch (SecurityException e) {
            throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      } else {
         JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR = null;
      }

   }
}
