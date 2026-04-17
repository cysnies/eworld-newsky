package com.mysql.jdbc.interceptors;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.StatementInterceptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultSetScannerInterceptor implements StatementInterceptor {
   private Pattern regexP;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$ResultSetInternalMethods;

   public void init(Connection conn, Properties props) throws SQLException {
      String regexFromUser = props.getProperty("resultSetScannerRegex");
      if (regexFromUser != null && regexFromUser.length() != 0) {
         try {
            this.regexP = Pattern.compile(regexFromUser);
         } catch (Throwable t) {
            SQLException sqlEx = new SQLException("Can't use configured regex due to underlying exception.");
            sqlEx.initCause(t);
            throw sqlEx;
         }
      } else {
         throw new SQLException("resultSetScannerRegex must be configured, and must be > 0 characters");
      }
   }

   public ResultSetInternalMethods postProcess(String sql, Statement interceptedStatement, final ResultSetInternalMethods originalResultSet, Connection connection) throws SQLException {
      return (ResultSetInternalMethods)Proxy.newProxyInstance(originalResultSet.getClass().getClassLoader(), new Class[]{class$com$mysql$jdbc$ResultSetInternalMethods == null ? (class$com$mysql$jdbc$ResultSetInternalMethods = class$("com.mysql.jdbc.ResultSetInternalMethods")) : class$com$mysql$jdbc$ResultSetInternalMethods}, new InvocationHandler() {
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object invocationResult = method.invoke(originalResultSet, args);
            String methodName = method.getName();
            if (invocationResult != null && invocationResult instanceof String || "getString".equals(methodName) || "getObject".equals(methodName) || "getObjectStoredProc".equals(methodName)) {
               Matcher matcher = ResultSetScannerInterceptor.this.regexP.matcher(invocationResult.toString());
               if (matcher.matches()) {
                  throw new SQLException("value disallowed by filter");
               }
            }

            return invocationResult;
         }
      });
   }

   public ResultSetInternalMethods preProcess(String sql, Statement interceptedStatement, Connection connection) throws SQLException {
      return null;
   }

   public boolean executeTopLevelOnly() {
      return false;
   }

   public void destroy() {
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }
}
