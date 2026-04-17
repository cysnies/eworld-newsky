package com.mysql.jdbc;

import java.sql.ParameterMetaData;
import java.sql.SQLException;

public class MysqlParameterMetadata implements ParameterMetaData {
   boolean returnSimpleMetadata = false;
   ResultSetMetaData metadata = null;
   int parameterCount = 0;

   MysqlParameterMetadata(Field[] fieldInfo, int parameterCount) {
      this.metadata = new ResultSetMetaData(fieldInfo, false);
      this.parameterCount = parameterCount;
   }

   MysqlParameterMetadata(int count) {
      this.parameterCount = count;
      this.returnSimpleMetadata = true;
   }

   public int getParameterCount() throws SQLException {
      return this.parameterCount;
   }

   public int isNullable(int arg0) throws SQLException {
      this.checkAvailable();
      return this.metadata.isNullable(arg0);
   }

   private void checkAvailable() throws SQLException {
      if (this.metadata == null || this.metadata.fields == null) {
         throw SQLError.createSQLException("Parameter metadata not available for the given statement", "S1C00");
      }
   }

   public boolean isSigned(int arg0) throws SQLException {
      if (this.returnSimpleMetadata) {
         this.checkBounds(arg0);
         return false;
      } else {
         this.checkAvailable();
         return this.metadata.isSigned(arg0);
      }
   }

   public int getPrecision(int arg0) throws SQLException {
      if (this.returnSimpleMetadata) {
         this.checkBounds(arg0);
         return 0;
      } else {
         this.checkAvailable();
         return this.metadata.getPrecision(arg0);
      }
   }

   public int getScale(int arg0) throws SQLException {
      if (this.returnSimpleMetadata) {
         this.checkBounds(arg0);
         return 0;
      } else {
         this.checkAvailable();
         return this.metadata.getScale(arg0);
      }
   }

   public int getParameterType(int arg0) throws SQLException {
      if (this.returnSimpleMetadata) {
         this.checkBounds(arg0);
         return 12;
      } else {
         this.checkAvailable();
         return this.metadata.getColumnType(arg0);
      }
   }

   public String getParameterTypeName(int arg0) throws SQLException {
      if (this.returnSimpleMetadata) {
         this.checkBounds(arg0);
         return "VARCHAR";
      } else {
         this.checkAvailable();
         return this.metadata.getColumnTypeName(arg0);
      }
   }

   public String getParameterClassName(int arg0) throws SQLException {
      if (this.returnSimpleMetadata) {
         this.checkBounds(arg0);
         return "java.lang.String";
      } else {
         this.checkAvailable();
         return this.metadata.getColumnClassName(arg0);
      }
   }

   public int getParameterMode(int arg0) throws SQLException {
      return 1;
   }

   private void checkBounds(int paramNumber) throws SQLException {
      if (paramNumber < 1) {
         throw SQLError.createSQLException("Parameter index of '" + paramNumber + "' is invalid.", "S1009");
      } else if (paramNumber > this.parameterCount) {
         throw SQLError.createSQLException("Parameter index of '" + paramNumber + "' is greater than number of parameters, which is '" + this.parameterCount + "'.", "S1009");
      }
   }

   public boolean isWrapperFor(Class iface) throws SQLException {
      return iface.isInstance(this);
   }

   public Object unwrap(Class iface) throws SQLException {
      try {
         return Util.cast(iface, this);
      } catch (ClassCastException var3) {
         throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009");
      }
   }
}
