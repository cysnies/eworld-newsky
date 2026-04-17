package com.mysql.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CallableStatement extends PreparedStatement implements java.sql.CallableStatement {
   protected static final Constructor JDBC_4_CSTMT_2_ARGS_CTOR;
   protected static final Constructor JDBC_4_CSTMT_4_ARGS_CTOR;
   private static final int NOT_OUTPUT_PARAMETER_INDICATOR = Integer.MIN_VALUE;
   private static final String PARAMETER_NAMESPACE_PREFIX = "@com_mysql_jdbc_outparam_";
   private boolean callingStoredFunction = false;
   private ResultSetInternalMethods functionReturnValueResults;
   private boolean hasOutputParams = false;
   private ResultSetInternalMethods outputParameterResults;
   protected boolean outputParamWasNull = false;
   private int[] parameterIndexToRsIndex;
   protected CallableStatementParamInfo paramInfo;
   private CallableStatementParam returnValueParam;
   private int[] placeholderToParameterIndexMap;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$ConnectionImpl;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$CallableStatement$CallableStatementParamInfo;
   // $FF: synthetic field
   static Class class$java$lang$String;

   private static String mangleParameterName(String origParameterName) {
      if (origParameterName == null) {
         return null;
      } else {
         int offset = 0;
         if (origParameterName.length() > 0 && origParameterName.charAt(0) == '@') {
            offset = 1;
         }

         StringBuffer paramNameBuf = new StringBuffer("@com_mysql_jdbc_outparam_".length() + origParameterName.length());
         paramNameBuf.append("@com_mysql_jdbc_outparam_");
         paramNameBuf.append(origParameterName.substring(offset));
         return paramNameBuf.toString();
      }
   }

   public CallableStatement(ConnectionImpl conn, CallableStatementParamInfo paramInfo) throws SQLException {
      super(conn, paramInfo.nativeSql, paramInfo.catalogInUse);
      this.paramInfo = paramInfo;
      this.callingStoredFunction = this.paramInfo.isFunctionCall;
      if (this.callingStoredFunction) {
         ++this.parameterCount;
      }

   }

   protected static CallableStatement getInstance(ConnectionImpl conn, String sql, String catalog, boolean isFunctionCall) throws SQLException {
      return !Util.isJdbc4() ? new CallableStatement(conn, sql, catalog, isFunctionCall) : (CallableStatement)Util.handleNewInstance(JDBC_4_CSTMT_4_ARGS_CTOR, new Object[]{conn, sql, catalog, isFunctionCall});
   }

   protected static CallableStatement getInstance(ConnectionImpl conn, CallableStatementParamInfo paramInfo) throws SQLException {
      return !Util.isJdbc4() ? new CallableStatement(conn, paramInfo) : (CallableStatement)Util.handleNewInstance(JDBC_4_CSTMT_2_ARGS_CTOR, new Object[]{conn, paramInfo});
   }

   private void generateParameterMap() throws SQLException {
      if (this.paramInfo != null) {
         int parameterCountFromMetaData = this.paramInfo.getParameterCount();
         if (this.callingStoredFunction) {
            --parameterCountFromMetaData;
         }

         if (this.paramInfo != null && this.parameterCount != parameterCountFromMetaData) {
            this.placeholderToParameterIndexMap = new int[this.parameterCount];
            int startPos = this.callingStoredFunction ? StringUtils.indexOfIgnoreCase(this.originalSql, "SELECT") : StringUtils.indexOfIgnoreCase(this.originalSql, "CALL");
            if (startPos != -1) {
               int parenOpenPos = this.originalSql.indexOf(40, startPos + 4);
               if (parenOpenPos != -1) {
                  int parenClosePos = StringUtils.indexOfIgnoreCaseRespectQuotes(parenOpenPos, this.originalSql, ")", '\'', true);
                  if (parenClosePos != -1) {
                     List parsedParameters = StringUtils.split(this.originalSql.substring(parenOpenPos + 1, parenClosePos), ",", "'\"", "'\"", true);
                     int numParsedParameters = parsedParameters.size();
                     if (numParsedParameters != this.parameterCount) {
                     }

                     int placeholderCount = 0;

                     for(int i = 0; i < numParsedParameters; ++i) {
                        if (((String)parsedParameters.get(i)).equals("?")) {
                           this.placeholderToParameterIndexMap[placeholderCount++] = i;
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public CallableStatement(ConnectionImpl conn, String sql, String catalog, boolean isFunctionCall) throws SQLException {
      super(conn, sql, catalog);
      this.callingStoredFunction = isFunctionCall;
      if (!this.callingStoredFunction) {
         if (!StringUtils.startsWithIgnoreCaseAndWs(sql, "CALL")) {
            this.fakeParameterTypes(false);
         } else {
            this.determineParameterTypes();
         }

         this.generateParameterMap();
      } else {
         this.determineParameterTypes();
         this.generateParameterMap();
         ++this.parameterCount;
      }

   }

   public void addBatch() throws SQLException {
      this.setOutParams();
      super.addBatch();
   }

   private CallableStatementParam checkIsOutputParam(int paramIndex) throws SQLException {
      if (this.callingStoredFunction) {
         if (paramIndex == 1) {
            if (this.returnValueParam == null) {
               this.returnValueParam = new CallableStatementParam("", 0, false, true, 12, "VARCHAR", 0, 0, (short)2, 5);
            }

            return this.returnValueParam;
         }

         --paramIndex;
      }

      this.checkParameterIndexBounds(paramIndex);
      int localParamIndex = paramIndex - 1;
      if (this.placeholderToParameterIndexMap != null) {
         localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
      }

      CallableStatementParam paramDescriptor = this.paramInfo.getParameter(localParamIndex);
      if (this.connection.getNoAccessToProcedureBodies()) {
         paramDescriptor.isOut = true;
         paramDescriptor.isIn = true;
         paramDescriptor.inOutModifier = 2;
      } else if (!paramDescriptor.isOut) {
         throw SQLError.createSQLException(Messages.getString("CallableStatement.9") + paramIndex + Messages.getString("CallableStatement.10"), "S1009");
      }

      this.hasOutputParams = true;
      return paramDescriptor;
   }

   private void checkParameterIndexBounds(int paramIndex) throws SQLException {
      this.paramInfo.checkBounds(paramIndex);
   }

   private void checkStreamability() throws SQLException {
      if (this.hasOutputParams && this.createStreamingResultSet()) {
         throw SQLError.createSQLException(Messages.getString("CallableStatement.14"), "S1C00");
      }
   }

   public synchronized void clearParameters() throws SQLException {
      super.clearParameters();

      try {
         if (this.outputParameterResults != null) {
            this.outputParameterResults.close();
         }
      } finally {
         this.outputParameterResults = null;
      }

   }

   private void fakeParameterTypes(boolean isReallyProcedure) throws SQLException {
      Field[] fields = new Field[]{new Field("", "PROCEDURE_CAT", 1, 0), new Field("", "PROCEDURE_SCHEM", 1, 0), new Field("", "PROCEDURE_NAME", 1, 0), new Field("", "COLUMN_NAME", 1, 0), new Field("", "COLUMN_TYPE", 1, 0), new Field("", "DATA_TYPE", 5, 0), new Field("", "TYPE_NAME", 1, 0), new Field("", "PRECISION", 4, 0), new Field("", "LENGTH", 4, 0), new Field("", "SCALE", 5, 0), new Field("", "RADIX", 5, 0), new Field("", "NULLABLE", 5, 0), new Field("", "REMARKS", 1, 0)};
      String procName = isReallyProcedure ? this.extractProcedureName() : null;
      byte[] procNameAsBytes = null;

      try {
         procNameAsBytes = procName == null ? null : procName.getBytes("UTF-8");
      } catch (UnsupportedEncodingException var8) {
         procNameAsBytes = StringUtils.s2b(procName, this.connection);
      }

      ArrayList resultRows = new ArrayList();

      for(int i = 0; i < this.parameterCount; ++i) {
         byte[][] row = new byte[][]{null, null, procNameAsBytes, StringUtils.s2b(String.valueOf(i), this.connection), StringUtils.s2b(String.valueOf(1), this.connection), StringUtils.s2b(String.valueOf(12), this.connection), StringUtils.s2b("VARCHAR", this.connection), StringUtils.s2b(Integer.toString(65535), this.connection), StringUtils.s2b(Integer.toString(65535), this.connection), StringUtils.s2b(Integer.toString(0), this.connection), StringUtils.s2b(Integer.toString(10), this.connection), StringUtils.s2b(Integer.toString(2), this.connection), null};
         resultRows.add(new ByteArrayRow(row));
      }

      ResultSet paramTypesRs = DatabaseMetaData.buildResultSet(fields, resultRows, this.connection);
      this.convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
   }

   private void determineParameterTypes() throws SQLException {
      if (this.connection.getNoAccessToProcedureBodies()) {
         this.fakeParameterTypes(true);
      } else {
         ResultSet paramTypesRs = null;

         try {
            String procName = this.extractProcedureName();
            java.sql.DatabaseMetaData dbmd = this.connection.getMetaData();
            boolean useCatalog = false;
            if (procName.indexOf(".") == -1) {
               useCatalog = true;
            }

            paramTypesRs = dbmd.getProcedureColumns(this.connection.versionMeetsMinimum(5, 0, 2) && useCatalog ? this.currentCatalog : null, (String)null, procName, "%");
            this.convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
         } finally {
            SQLException sqlExRethrow = null;
            if (paramTypesRs != null) {
               try {
                  paramTypesRs.close();
               } catch (SQLException sqlEx) {
                  sqlExRethrow = sqlEx;
               }

               ResultSet var14 = null;
            }

            if (sqlExRethrow != null) {
               throw sqlExRethrow;
            }

         }

      }
   }

   private void convertGetProcedureColumnsToInternalDescriptors(ResultSet paramTypesRs) throws SQLException {
      if (!this.connection.isRunningOnJDK13()) {
         this.paramInfo = new CallableStatementParamInfoJDBC3(paramTypesRs);
      } else {
         this.paramInfo = new CallableStatementParamInfo(paramTypesRs);
      }

   }

   public boolean execute() throws SQLException {
      boolean returnVal = false;
      this.checkClosed();
      this.checkStreamability();
      synchronized(this.connection.getMutex()) {
         this.setInOutParamsOnServer();
         this.setOutParams();
         returnVal = super.execute();
         if (this.callingStoredFunction) {
            this.functionReturnValueResults = this.results;
            this.functionReturnValueResults.next();
            this.results = null;
         }

         this.retrieveOutParams();
      }

      return !this.callingStoredFunction ? returnVal : false;
   }

   public ResultSet executeQuery() throws SQLException {
      this.checkClosed();
      this.checkStreamability();
      ResultSet execResults = null;
      synchronized(this.connection.getMutex()) {
         this.setInOutParamsOnServer();
         this.setOutParams();
         execResults = super.executeQuery();
         this.retrieveOutParams();
         return execResults;
      }
   }

   public int executeUpdate() throws SQLException {
      int returnVal = -1;
      this.checkClosed();
      this.checkStreamability();
      if (this.callingStoredFunction) {
         this.execute();
         return -1;
      } else {
         synchronized(this.connection.getMutex()) {
            this.setInOutParamsOnServer();
            this.setOutParams();
            returnVal = super.executeUpdate();
            this.retrieveOutParams();
            return returnVal;
         }
      }
   }

   private String extractProcedureName() throws SQLException {
      String sanitizedSql = StringUtils.stripComments(this.originalSql, "`\"'", "`\"'", true, false, true, true);
      int endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "CALL ");
      int offset = 5;
      if (endCallIndex == -1) {
         endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "SELECT ");
         offset = 7;
      }

      if (endCallIndex == -1) {
         throw SQLError.createSQLException(Messages.getString("CallableStatement.1"), "S1000");
      } else {
         StringBuffer nameBuf = new StringBuffer();
         String trimmedStatement = sanitizedSql.substring(endCallIndex + offset).trim();
         int statementLength = trimmedStatement.length();

         for(int i = 0; i < statementLength; ++i) {
            char c = trimmedStatement.charAt(i);
            if (Character.isWhitespace(c) || c == '(' || c == '?') {
               break;
            }

            nameBuf.append(c);
         }

         return nameBuf.toString();
      }
   }

   protected String fixParameterName(String paramNameIn) throws SQLException {
      if (paramNameIn != null && paramNameIn.length() != 0) {
         if (this.connection.getNoAccessToProcedureBodies()) {
            throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", "S1009");
         } else {
            return mangleParameterName(paramNameIn);
         }
      } else {
         throw SQLError.createSQLException(Messages.getString("CallableStatement.0") + paramNameIn == null ? Messages.getString("CallableStatement.15") : Messages.getString("CallableStatement.16"), "S1009");
      }
   }

   public synchronized Array getArray(int i) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(i);
      Array retValue = rs.getArray(this.mapOutputParameterIndexToRsIndex(i));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Array getArray(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Array retValue = rs.getArray(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      BigDecimal retValue = rs.getBigDecimal(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   /** @deprecated */
   public synchronized BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      BigDecimal retValue = rs.getBigDecimal(this.mapOutputParameterIndexToRsIndex(parameterIndex), scale);
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized BigDecimal getBigDecimal(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      BigDecimal retValue = rs.getBigDecimal(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized java.sql.Blob getBlob(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      java.sql.Blob retValue = rs.getBlob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized java.sql.Blob getBlob(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      java.sql.Blob retValue = rs.getBlob(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized boolean getBoolean(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      boolean retValue = rs.getBoolean(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized boolean getBoolean(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      boolean retValue = rs.getBoolean(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized byte getByte(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      byte retValue = rs.getByte(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized byte getByte(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      byte retValue = rs.getByte(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized byte[] getBytes(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      byte[] retValue = rs.getBytes(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized byte[] getBytes(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      byte[] retValue = rs.getBytes(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized java.sql.Clob getClob(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      java.sql.Clob retValue = rs.getClob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized java.sql.Clob getClob(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      java.sql.Clob retValue = rs.getClob(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Date getDate(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      Date retValue = rs.getDate(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Date getDate(int parameterIndex, Calendar cal) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      Date retValue = rs.getDate(this.mapOutputParameterIndexToRsIndex(parameterIndex), cal);
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Date getDate(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Date retValue = rs.getDate(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Date getDate(String parameterName, Calendar cal) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Date retValue = rs.getDate(this.fixParameterName(parameterName), cal);
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized double getDouble(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      double retValue = rs.getDouble(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized double getDouble(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      double retValue = rs.getDouble(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized float getFloat(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      float retValue = rs.getFloat(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized float getFloat(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      float retValue = rs.getFloat(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized int getInt(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      int retValue = rs.getInt(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized int getInt(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      int retValue = rs.getInt(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized long getLong(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      long retValue = rs.getLong(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized long getLong(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      long retValue = rs.getLong(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   protected int getNamedParamIndex(String paramName, boolean forOut) throws SQLException {
      if (this.connection.getNoAccessToProcedureBodies()) {
         throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", "S1009");
      } else if (paramName != null && paramName.length() != 0) {
         if (this.paramInfo == null) {
            throw SQLError.createSQLException(Messages.getString("CallableStatement.3") + paramName + Messages.getString("CallableStatement.4"), "S1009");
         } else {
            CallableStatementParam namedParamInfo = this.paramInfo.getParameter(paramName);
            if (forOut && !namedParamInfo.isOut) {
               throw SQLError.createSQLException(Messages.getString("CallableStatement.5") + paramName + Messages.getString("CallableStatement.6"), "S1009");
            } else if (this.placeholderToParameterIndexMap == null) {
               return namedParamInfo.index + 1;
            } else {
               for(int i = 0; i < this.placeholderToParameterIndexMap.length; ++i) {
                  if (this.placeholderToParameterIndexMap[i] == namedParamInfo.index) {
                     return i + 1;
                  }
               }

               throw SQLError.createSQLException("Can't find local placeholder mapping for parameter named \"" + paramName + "\".", "S1009");
            }
         }
      } else {
         throw SQLError.createSQLException(Messages.getString("CallableStatement.2"), "S1009");
      }
   }

   public synchronized Object getObject(int parameterIndex) throws SQLException {
      CallableStatementParam paramDescriptor = this.checkIsOutputParam(parameterIndex);
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      Object retVal = rs.getObjectStoredProc(this.mapOutputParameterIndexToRsIndex(parameterIndex), paramDescriptor.desiredJdbcType);
      this.outputParamWasNull = rs.wasNull();
      return retVal;
   }

   public synchronized Object getObject(int parameterIndex, Map map) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      Object retVal = rs.getObject(this.mapOutputParameterIndexToRsIndex(parameterIndex), map);
      this.outputParamWasNull = rs.wasNull();
      return retVal;
   }

   public synchronized Object getObject(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Object retValue = rs.getObject(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Object getObject(String parameterName, Map map) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Object retValue = rs.getObject(this.fixParameterName(parameterName), map);
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   protected ResultSetInternalMethods getOutputParameters(int paramIndex) throws SQLException {
      this.outputParamWasNull = false;
      if (paramIndex == 1 && this.callingStoredFunction && this.returnValueParam != null) {
         return this.functionReturnValueResults;
      } else if (this.outputParameterResults == null) {
         if (this.paramInfo.numberOfParameters() == 0) {
            throw SQLError.createSQLException(Messages.getString("CallableStatement.7"), "S1009");
         } else {
            throw SQLError.createSQLException(Messages.getString("CallableStatement.8"), "S1000");
         }
      } else {
         return this.outputParameterResults;
      }
   }

   public synchronized ParameterMetaData getParameterMetaData() throws SQLException {
      return this.placeholderToParameterIndexMap == null ? (CallableStatementParamInfoJDBC3)this.paramInfo : new CallableStatementParamInfoJDBC3(this.paramInfo);
   }

   public synchronized Ref getRef(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      Ref retValue = rs.getRef(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Ref getRef(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Ref retValue = rs.getRef(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized short getShort(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      short retValue = rs.getShort(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized short getShort(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      short retValue = rs.getShort(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized String getString(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      String retValue = rs.getString(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized String getString(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      String retValue = rs.getString(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Time getTime(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      Time retValue = rs.getTime(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Time getTime(int parameterIndex, Calendar cal) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      Time retValue = rs.getTime(this.mapOutputParameterIndexToRsIndex(parameterIndex), cal);
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Time getTime(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Time retValue = rs.getTime(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Time getTime(String parameterName, Calendar cal) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Time retValue = rs.getTime(this.fixParameterName(parameterName), cal);
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Timestamp getTimestamp(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      Timestamp retValue = rs.getTimestamp(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      Timestamp retValue = rs.getTimestamp(this.mapOutputParameterIndexToRsIndex(parameterIndex), cal);
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Timestamp getTimestamp(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Timestamp retValue = rs.getTimestamp(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      Timestamp retValue = rs.getTimestamp(this.fixParameterName(parameterName), cal);
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized URL getURL(int parameterIndex) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
      URL retValue = rs.getURL(this.mapOutputParameterIndexToRsIndex(parameterIndex));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   public synchronized URL getURL(String parameterName) throws SQLException {
      ResultSetInternalMethods rs = this.getOutputParameters(0);
      URL retValue = rs.getURL(this.fixParameterName(parameterName));
      this.outputParamWasNull = rs.wasNull();
      return retValue;
   }

   protected int mapOutputParameterIndexToRsIndex(int paramIndex) throws SQLException {
      if (this.returnValueParam != null && paramIndex == 1) {
         return 1;
      } else {
         this.checkParameterIndexBounds(paramIndex);
         int localParamIndex = paramIndex - 1;
         if (this.placeholderToParameterIndexMap != null) {
            localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
         }

         int rsIndex = this.parameterIndexToRsIndex[localParamIndex];
         if (rsIndex == Integer.MIN_VALUE) {
            throw SQLError.createSQLException(Messages.getString("CallableStatement.21") + paramIndex + Messages.getString("CallableStatement.22"), "S1009");
         } else {
            return rsIndex + 1;
         }
      }
   }

   public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
      CallableStatementParam paramDescriptor = this.checkIsOutputParam(parameterIndex);
      paramDescriptor.desiredJdbcType = sqlType;
   }

   public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
      this.registerOutParameter(parameterIndex, sqlType);
   }

   public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
      this.checkIsOutputParam(parameterIndex);
   }

   public synchronized void registerOutParameter(String parameterName, int sqlType) throws SQLException {
      this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType);
   }

   public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
      this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType);
   }

   public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
      this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType, typeName);
   }

   private void retrieveOutParams() throws SQLException {
      int numParameters = this.paramInfo.numberOfParameters();
      this.parameterIndexToRsIndex = new int[numParameters];

      for(int i = 0; i < numParameters; ++i) {
         this.parameterIndexToRsIndex[i] = Integer.MIN_VALUE;
      }

      int localParamIndex = 0;
      if (numParameters > 0) {
         StringBuffer outParameterQuery = new StringBuffer("SELECT ");
         boolean firstParam = true;
         boolean hadOutputParams = false;

         for(CallableStatementParam retrParamInfo : this.paramInfo) {
            if (retrParamInfo.isOut) {
               hadOutputParams = true;
               this.parameterIndexToRsIndex[retrParamInfo.index] = localParamIndex++;
               String outParameterName = mangleParameterName(retrParamInfo.paramName);
               if (!firstParam) {
                  outParameterQuery.append(",");
               } else {
                  firstParam = false;
               }

               if (!outParameterName.startsWith("@")) {
                  outParameterQuery.append('@');
               }

               outParameterQuery.append(outParameterName);
            }
         }

         if (hadOutputParams) {
            java.sql.Statement outParameterStmt = null;
            ResultSet outParamRs = null;

            try {
               outParameterStmt = this.connection.createStatement();
               outParamRs = outParameterStmt.executeQuery(outParameterQuery.toString());
               this.outputParameterResults = ((ResultSetInternalMethods)outParamRs).copy();
               if (!this.outputParameterResults.next()) {
                  this.outputParameterResults.close();
                  this.outputParameterResults = null;
               }
            } finally {
               if (outParameterStmt != null) {
                  outParameterStmt.close();
               }

            }
         } else {
            this.outputParameterResults = null;
         }
      } else {
         this.outputParameterResults = null;
      }

   }

   public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
      this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x, length);
   }

   public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
      this.setBigDecimal(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
      this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x, length);
   }

   public void setBoolean(String parameterName, boolean x) throws SQLException {
      this.setBoolean(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setByte(String parameterName, byte x) throws SQLException {
      this.setByte(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setBytes(String parameterName, byte[] x) throws SQLException {
      this.setBytes(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
      this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader, length);
   }

   public void setDate(String parameterName, Date x) throws SQLException {
      this.setDate(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
      this.setDate(this.getNamedParamIndex(parameterName, false), x, cal);
   }

   public void setDouble(String parameterName, double x) throws SQLException {
      this.setDouble(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setFloat(String parameterName, float x) throws SQLException {
      this.setFloat(this.getNamedParamIndex(parameterName, false), x);
   }

   private void setInOutParamsOnServer() throws SQLException {
      if (this.paramInfo.numParameters > 0) {
         int parameterIndex = 0;

         for(CallableStatementParam inParamInfo : this.paramInfo) {
            if (inParamInfo.isOut && inParamInfo.isIn) {
               String inOutParameterName = mangleParameterName(inParamInfo.paramName);
               StringBuffer queryBuf = new StringBuffer(4 + inOutParameterName.length() + 1 + 1);
               queryBuf.append("SET ");
               queryBuf.append(inOutParameterName);
               queryBuf.append("=?");
               PreparedStatement setPstmt = null;

               try {
                  setPstmt = (PreparedStatement)this.connection.clientPrepareStatement(queryBuf.toString());
                  byte[] parameterAsBytes = this.getBytesRepresentation(inParamInfo.index);
                  if (parameterAsBytes != null) {
                     if (parameterAsBytes.length > 8 && parameterAsBytes[0] == 95 && parameterAsBytes[1] == 98 && parameterAsBytes[2] == 105 && parameterAsBytes[3] == 110 && parameterAsBytes[4] == 97 && parameterAsBytes[5] == 114 && parameterAsBytes[6] == 121 && parameterAsBytes[7] == 39) {
                        setPstmt.setBytesNoEscapeNoQuotes(1, parameterAsBytes);
                     } else {
                        int sqlType = inParamInfo.desiredJdbcType;
                        switch (sqlType) {
                           case -7:
                           case -4:
                           case -3:
                           case -2:
                           case 2000:
                           case 2004:
                              setPstmt.setBytes(1, parameterAsBytes);
                              break;
                           default:
                              setPstmt.setBytesNoEscape(1, parameterAsBytes);
                        }
                     }
                  } else {
                     setPstmt.setNull(1, 0);
                  }

                  setPstmt.executeUpdate();
               } finally {
                  if (setPstmt != null) {
                     setPstmt.close();
                  }

               }
            }

            ++parameterIndex;
         }
      }

   }

   public void setInt(String parameterName, int x) throws SQLException {
      this.setInt(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setLong(String parameterName, long x) throws SQLException {
      this.setLong(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setNull(String parameterName, int sqlType) throws SQLException {
      this.setNull(this.getNamedParamIndex(parameterName, false), sqlType);
   }

   public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
      this.setNull(this.getNamedParamIndex(parameterName, false), sqlType, typeName);
   }

   public void setObject(String parameterName, Object x) throws SQLException {
      this.setObject(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
      this.setObject(this.getNamedParamIndex(parameterName, false), x, targetSqlType);
   }

   public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
   }

   private void setOutParams() throws SQLException {
      if (this.paramInfo.numParameters > 0) {
         for(CallableStatementParam outParamInfo : this.paramInfo) {
            if (!this.callingStoredFunction && outParamInfo.isOut) {
               String outParameterName = mangleParameterName(outParamInfo.paramName);
               int outParamIndex;
               if (this.placeholderToParameterIndexMap == null) {
                  outParamIndex = outParamInfo.index + 1;
               } else {
                  outParamIndex = this.placeholderToParameterIndexMap[outParamInfo.index - 1];
               }

               this.setBytesNoEscapeNoQuotes(outParamIndex, StringUtils.getBytes(outParameterName, this.charConverter, this.charEncoding, this.connection.getServerCharacterEncoding(), this.connection.parserKnowsUnicode()));
            }
         }
      }

   }

   public void setShort(String parameterName, short x) throws SQLException {
      this.setShort(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setString(String parameterName, String x) throws SQLException {
      this.setString(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setTime(String parameterName, Time x) throws SQLException {
      this.setTime(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
      this.setTime(this.getNamedParamIndex(parameterName, false), x, cal);
   }

   public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
      this.setTimestamp(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
      this.setTimestamp(this.getNamedParamIndex(parameterName, false), x, cal);
   }

   public void setURL(String parameterName, URL val) throws SQLException {
      this.setURL(this.getNamedParamIndex(parameterName, false), val);
   }

   public synchronized boolean wasNull() throws SQLException {
      return this.outputParamWasNull;
   }

   public int[] executeBatch() throws SQLException {
      if (this.hasOutputParams) {
         throw SQLError.createSQLException("Can't call executeBatch() on CallableStatement with OUTPUT parameters", "S1009");
      } else {
         return super.executeBatch();
      }
   }

   protected int getParameterIndexOffset() {
      return this.callingStoredFunction ? -1 : super.getParameterIndexOffset();
   }

   public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
      this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
      this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x, length);
   }

   public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
      this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
      this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x, length);
   }

   public void setBlob(String parameterName, java.sql.Blob x) throws SQLException {
      this.setBlob(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
      this.setBlob(this.getNamedParamIndex(parameterName, false), inputStream);
   }

   public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
      this.setBlob(this.getNamedParamIndex(parameterName, false), inputStream, length);
   }

   public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
      this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader);
   }

   public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
      this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader, length);
   }

   public void setClob(String parameterName, java.sql.Clob x) throws SQLException {
      this.setClob(this.getNamedParamIndex(parameterName, false), x);
   }

   public void setClob(String parameterName, Reader reader) throws SQLException {
      this.setClob(this.getNamedParamIndex(parameterName, false), reader);
   }

   public void setClob(String parameterName, Reader reader, long length) throws SQLException {
      this.setClob(this.getNamedParamIndex(parameterName, false), reader, length);
   }

   public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
      this.setNCharacterStream(this.getNamedParamIndex(parameterName, false), value);
   }

   public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
      this.setNCharacterStream(this.getNamedParamIndex(parameterName, false), value, length);
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
            JDBC_4_CSTMT_2_ARGS_CTOR = Class.forName("com.mysql.jdbc.JDBC4CallableStatement").getConstructor(class$com$mysql$jdbc$ConnectionImpl == null ? (class$com$mysql$jdbc$ConnectionImpl = class$("com.mysql.jdbc.ConnectionImpl")) : class$com$mysql$jdbc$ConnectionImpl, class$com$mysql$jdbc$CallableStatement$CallableStatementParamInfo == null ? (class$com$mysql$jdbc$CallableStatement$CallableStatementParamInfo = class$("com.mysql.jdbc.CallableStatement$CallableStatementParamInfo")) : class$com$mysql$jdbc$CallableStatement$CallableStatementParamInfo);
            JDBC_4_CSTMT_4_ARGS_CTOR = Class.forName("com.mysql.jdbc.JDBC4CallableStatement").getConstructor(class$com$mysql$jdbc$ConnectionImpl == null ? (class$com$mysql$jdbc$ConnectionImpl = class$("com.mysql.jdbc.ConnectionImpl")) : class$com$mysql$jdbc$ConnectionImpl, class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, class$java$lang$String == null ? (class$java$lang$String = class$("java.lang.String")) : class$java$lang$String, Boolean.TYPE);
         } catch (SecurityException e) {
            throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      } else {
         JDBC_4_CSTMT_4_ARGS_CTOR = null;
         JDBC_4_CSTMT_2_ARGS_CTOR = null;
      }

   }

   protected class CallableStatementParam {
      int desiredJdbcType;
      int index;
      int inOutModifier;
      boolean isIn;
      boolean isOut;
      int jdbcType;
      short nullability;
      String paramName;
      int precision;
      int scale;
      String typeName;

      CallableStatementParam(String name, int idx, boolean in, boolean out, int jdbcType, String typeName, int precision, int scale, short nullability, int inOutModifier) {
         this.paramName = name;
         this.isIn = in;
         this.isOut = out;
         this.index = idx;
         this.jdbcType = jdbcType;
         this.typeName = typeName;
         this.precision = precision;
         this.scale = scale;
         this.nullability = nullability;
         this.inOutModifier = inOutModifier;
      }

      protected Object clone() throws CloneNotSupportedException {
         return super.clone();
      }
   }

   protected class CallableStatementParamInfo {
      String catalogInUse;
      boolean isFunctionCall;
      String nativeSql;
      int numParameters;
      List parameterList;
      Map parameterMap;

      CallableStatementParamInfo(CallableStatementParamInfo fullParamInfo) {
         this.nativeSql = CallableStatement.this.originalSql;
         this.catalogInUse = CallableStatement.this.currentCatalog;
         this.isFunctionCall = fullParamInfo.isFunctionCall;
         int[] localParameterMap = CallableStatement.this.placeholderToParameterIndexMap;
         int parameterMapLength = localParameterMap.length;
         this.parameterList = new ArrayList(fullParamInfo.numParameters);
         this.parameterMap = new HashMap(fullParamInfo.numParameters);
         if (this.isFunctionCall) {
            this.parameterList.add(fullParamInfo.parameterList.get(0));
         }

         int offset = this.isFunctionCall ? 1 : 0;

         for(int i = 0; i < parameterMapLength; ++i) {
            if (localParameterMap[i] != 0) {
               CallableStatementParam param = (CallableStatementParam)fullParamInfo.parameterList.get(localParameterMap[i] + offset);
               this.parameterList.add(param);
               this.parameterMap.put(param.paramName, param);
            }
         }

         this.numParameters = this.parameterList.size();
      }

      CallableStatementParamInfo(ResultSet paramTypesRs) throws SQLException {
         boolean hadRows = paramTypesRs.last();
         this.nativeSql = CallableStatement.this.originalSql;
         this.catalogInUse = CallableStatement.this.currentCatalog;
         this.isFunctionCall = CallableStatement.this.callingStoredFunction;
         if (hadRows) {
            this.numParameters = paramTypesRs.getRow();
            this.parameterList = new ArrayList(this.numParameters);
            this.parameterMap = new HashMap(this.numParameters);
            paramTypesRs.beforeFirst();
            this.addParametersFromDBMD(paramTypesRs);
         } else {
            this.numParameters = 0;
         }

         if (this.isFunctionCall) {
            ++this.numParameters;
         }

      }

      private void addParametersFromDBMD(ResultSet paramTypesRs) throws SQLException {
         int i = 0;

         while(paramTypesRs.next()) {
            String paramName = paramTypesRs.getString(4);
            int inOutModifier = paramTypesRs.getInt(5);
            boolean isOutParameter = false;
            boolean isInParameter = false;
            if (i == 0 && this.isFunctionCall) {
               isOutParameter = true;
               isInParameter = false;
            } else if (inOutModifier == 2) {
               isOutParameter = true;
               isInParameter = true;
            } else if (inOutModifier == 1) {
               isOutParameter = false;
               isInParameter = true;
            } else if (inOutModifier == 4) {
               isOutParameter = true;
               isInParameter = false;
            }

            int jdbcType = paramTypesRs.getInt(6);
            String typeName = paramTypesRs.getString(7);
            int precision = paramTypesRs.getInt(8);
            int scale = paramTypesRs.getInt(10);
            short nullability = paramTypesRs.getShort(12);
            CallableStatementParam paramInfoToAdd = CallableStatement.this.new CallableStatementParam(paramName, i++, isInParameter, isOutParameter, jdbcType, typeName, precision, scale, nullability, inOutModifier);
            this.parameterList.add(paramInfoToAdd);
            this.parameterMap.put(paramName, paramInfoToAdd);
         }

      }

      protected void checkBounds(int paramIndex) throws SQLException {
         int localParamIndex = paramIndex - 1;
         if (paramIndex < 0 || localParamIndex >= this.numParameters) {
            throw SQLError.createSQLException(Messages.getString("CallableStatement.11") + paramIndex + Messages.getString("CallableStatement.12") + this.numParameters + Messages.getString("CallableStatement.13"), "S1009");
         }
      }

      protected Object clone() throws CloneNotSupportedException {
         return super.clone();
      }

      CallableStatementParam getParameter(int index) {
         return (CallableStatementParam)this.parameterList.get(index);
      }

      CallableStatementParam getParameter(String name) {
         return (CallableStatementParam)this.parameterMap.get(name);
      }

      public String getParameterClassName(int arg0) throws SQLException {
         String mysqlTypeName = this.getParameterTypeName(arg0);
         boolean isBinaryOrBlob = StringUtils.indexOfIgnoreCase(mysqlTypeName, "BLOB") != -1 || StringUtils.indexOfIgnoreCase(mysqlTypeName, "BINARY") != -1;
         boolean isUnsigned = StringUtils.indexOfIgnoreCase(mysqlTypeName, "UNSIGNED") != -1;
         int mysqlTypeIfKnown = 0;
         if (StringUtils.startsWithIgnoreCase(mysqlTypeName, "MEDIUMINT")) {
            mysqlTypeIfKnown = 9;
         }

         return ResultSetMetaData.getClassNameForJavaType(this.getParameterType(arg0), isUnsigned, mysqlTypeIfKnown, isBinaryOrBlob, false);
      }

      public int getParameterCount() throws SQLException {
         return this.parameterList == null ? 0 : this.parameterList.size();
      }

      public int getParameterMode(int arg0) throws SQLException {
         this.checkBounds(arg0);
         return this.getParameter(arg0 - 1).inOutModifier;
      }

      public int getParameterType(int arg0) throws SQLException {
         this.checkBounds(arg0);
         return this.getParameter(arg0 - 1).jdbcType;
      }

      public String getParameterTypeName(int arg0) throws SQLException {
         this.checkBounds(arg0);
         return this.getParameter(arg0 - 1).typeName;
      }

      public int getPrecision(int arg0) throws SQLException {
         this.checkBounds(arg0);
         return this.getParameter(arg0 - 1).precision;
      }

      public int getScale(int arg0) throws SQLException {
         this.checkBounds(arg0);
         return this.getParameter(arg0 - 1).scale;
      }

      public int isNullable(int arg0) throws SQLException {
         this.checkBounds(arg0);
         return this.getParameter(arg0 - 1).nullability;
      }

      public boolean isSigned(int arg0) throws SQLException {
         this.checkBounds(arg0);
         return false;
      }

      Iterator iterator() {
         return this.parameterList.iterator();
      }

      int numberOfParameters() {
         return this.numParameters;
      }
   }

   protected class CallableStatementParamInfoJDBC3 extends CallableStatementParamInfo implements ParameterMetaData {
      CallableStatementParamInfoJDBC3(ResultSet paramTypesRs) throws SQLException {
         super((ResultSet)paramTypesRs);
      }

      public CallableStatementParamInfoJDBC3(CallableStatementParamInfo paramInfo) {
         super((CallableStatementParamInfo)paramInfo);
      }

      public boolean isWrapperFor(Class iface) throws SQLException {
         CallableStatement.this.checkClosed();
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
}
