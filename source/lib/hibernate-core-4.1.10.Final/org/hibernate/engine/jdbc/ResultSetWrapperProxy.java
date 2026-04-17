package org.hibernate.engine.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class ResultSetWrapperProxy implements InvocationHandler {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ResultSetWrapperProxy.class.getName());
   private static final Class[] PROXY_INTERFACES = new Class[]{ResultSet.class};
   private static final SqlExceptionHelper sqlExceptionHelper = new SqlExceptionHelper();
   private final ResultSet rs;
   private final ColumnNameCache columnNameCache;

   private ResultSetWrapperProxy(ResultSet rs, ColumnNameCache columnNameCache) {
      this.rs = rs;
      this.columnNameCache = columnNameCache;
   }

   public static ResultSet generateProxy(ResultSet resultSet, ColumnNameCache columnNameCache) {
      return (ResultSet)Proxy.newProxyInstance(getProxyClassLoader(), PROXY_INTERFACES, new ResultSetWrapperProxy(resultSet, columnNameCache));
   }

   public static ClassLoader getProxyClassLoader() {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (cl == null) {
         cl = ResultSet.class.getClassLoader();
      }

      return cl;
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if ("findColumn".equals(method.getName())) {
         return this.findColumn((String)args[0]);
      } else {
         if (this.isFirstArgColumnLabel(method, args)) {
            try {
               int columnIndex = this.findColumn((String)args[0]);
               return this.invokeMethod(this.locateCorrespondingColumnIndexMethod(method), this.buildColumnIndexMethodArgs(args, columnIndex));
            } catch (SQLException ex) {
               StringBuilder buf = (new StringBuilder()).append("Exception getting column index for column: [").append(args[0]).append("].\nReverting to using: [").append(args[0]).append("] as first argument for method: [").append(method).append("]");
               sqlExceptionHelper.logExceptions(ex, buf.toString());
            } catch (NoSuchMethodException var7) {
               LOG.unableToSwitchToMethodUsingColumnIndex(method);
            }
         }

         return this.invokeMethod(method, args);
      }
   }

   private int findColumn(String columnName) throws SQLException {
      return this.columnNameCache.getIndexForColumnName(columnName, this.rs);
   }

   private boolean isFirstArgColumnLabel(Method method, Object[] args) {
      if (!method.getName().startsWith("get") && !method.getName().startsWith("update")) {
         return false;
      } else if (method.getParameterTypes().length > 0 && args.length == method.getParameterTypes().length) {
         return String.class.isInstance(args[0]) && method.getParameterTypes()[0].equals(String.class);
      } else {
         return false;
      }
   }

   private Method locateCorrespondingColumnIndexMethod(Method columnNameMethod) throws NoSuchMethodException {
      Class[] actualParameterTypes = new Class[columnNameMethod.getParameterTypes().length];
      actualParameterTypes[0] = Integer.TYPE;
      System.arraycopy(columnNameMethod.getParameterTypes(), 1, actualParameterTypes, 1, columnNameMethod.getParameterTypes().length - 1);
      return columnNameMethod.getDeclaringClass().getMethod(columnNameMethod.getName(), actualParameterTypes);
   }

   private Object[] buildColumnIndexMethodArgs(Object[] incomingArgs, int columnIndex) {
      Object[] actualArgs = new Object[incomingArgs.length];
      actualArgs[0] = columnIndex;
      System.arraycopy(incomingArgs, 1, actualArgs, 1, incomingArgs.length - 1);
      return actualArgs;
   }

   private Object invokeMethod(Method method, Object[] args) throws Throwable {
      try {
         return method.invoke(this.rs, args);
      } catch (InvocationTargetException e) {
         throw e.getTargetException();
      }
   }
}
