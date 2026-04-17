package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.hibernate.engine.jdbc.spi.InvalidatableWrapper;
import org.hibernate.engine.jdbc.spi.JdbcWrapper;
import org.hibernate.engine.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.internal.util.ValueHolder;

public class ProxyBuilder {
   public static final Class[] CONNECTION_PROXY_INTERFACES = new Class[]{Connection.class, JdbcWrapper.class};
   private static final ValueHolder connectionProxyConstructorValue = new ValueHolder(new ValueHolder.DeferredInitializer() {
      public Constructor initialize() {
         try {
            return this.locateConnectionProxyClass().getConstructor(InvocationHandler.class);
         } catch (NoSuchMethodException e) {
            throw new JdbcProxyException("Could not find proxy constructor in JDK generated Connection proxy class", e);
         }
      }

      private Class locateConnectionProxyClass() {
         return Proxy.getProxyClass(JdbcWrapper.class.getClassLoader(), ProxyBuilder.CONNECTION_PROXY_INTERFACES);
      }
   });
   public static final Class[] STMNT_PROXY_INTERFACES = new Class[]{Statement.class, JdbcWrapper.class, InvalidatableWrapper.class};
   private static final ValueHolder statementProxyConstructorValue = new ValueHolder(new ValueHolder.DeferredInitializer() {
      public Constructor initialize() {
         try {
            return this.locateStatementProxyClass().getConstructor(InvocationHandler.class);
         } catch (NoSuchMethodException e) {
            throw new JdbcProxyException("Could not find proxy constructor in JDK generated Statement proxy class", e);
         }
      }

      private Class locateStatementProxyClass() {
         return Proxy.getProxyClass(JdbcWrapper.class.getClassLoader(), ProxyBuilder.STMNT_PROXY_INTERFACES);
      }
   });
   public static final Class[] PREPARED_STMNT_PROXY_INTERFACES = new Class[]{PreparedStatement.class, JdbcWrapper.class, InvalidatableWrapper.class};
   private static final ValueHolder preparedStatementProxyConstructorValue = new ValueHolder(new ValueHolder.DeferredInitializer() {
      public Constructor initialize() {
         try {
            return this.locatePreparedStatementProxyClass().getConstructor(InvocationHandler.class);
         } catch (NoSuchMethodException e) {
            throw new JdbcProxyException("Could not find proxy constructor in JDK generated Statement proxy class", e);
         }
      }

      private Class locatePreparedStatementProxyClass() {
         return Proxy.getProxyClass(JdbcWrapper.class.getClassLoader(), ProxyBuilder.PREPARED_STMNT_PROXY_INTERFACES);
      }
   });
   public static final Class[] CALLABLE_STMNT_PROXY_INTERFACES = new Class[]{CallableStatement.class, JdbcWrapper.class, InvalidatableWrapper.class};
   private static final ValueHolder callableStatementProxyConstructorValue = new ValueHolder(new ValueHolder.DeferredInitializer() {
      public Constructor initialize() {
         try {
            return this.locateCallableStatementProxyClass().getConstructor(InvocationHandler.class);
         } catch (NoSuchMethodException e) {
            throw new JdbcProxyException("Could not find proxy constructor in JDK generated Statement proxy class", e);
         }
      }

      private Class locateCallableStatementProxyClass() {
         return Proxy.getProxyClass(JdbcWrapper.class.getClassLoader(), ProxyBuilder.CALLABLE_STMNT_PROXY_INTERFACES);
      }
   });
   public static final Class[] RESULTSET_PROXY_INTERFACES = new Class[]{ResultSet.class, JdbcWrapper.class, InvalidatableWrapper.class};
   private static final ValueHolder resultSetProxyConstructorValue = new ValueHolder(new ValueHolder.DeferredInitializer() {
      public Constructor initialize() {
         try {
            return this.locateCallableStatementProxyClass().getConstructor(InvocationHandler.class);
         } catch (NoSuchMethodException e) {
            throw new JdbcProxyException("Could not find proxy constructor in JDK generated ResultSet proxy class", e);
         }
      }

      private Class locateCallableStatementProxyClass() {
         return Proxy.getProxyClass(JdbcWrapper.class.getClassLoader(), ProxyBuilder.RESULTSET_PROXY_INTERFACES);
      }
   });
   public static final Class[] METADATA_PROXY_INTERFACES = new Class[]{DatabaseMetaData.class, JdbcWrapper.class};
   private static final ValueHolder metadataProxyConstructorValue = new ValueHolder(new ValueHolder.DeferredInitializer() {
      public Constructor initialize() {
         try {
            return this.locateDatabaseMetaDataProxyClass().getConstructor(InvocationHandler.class);
         } catch (NoSuchMethodException e) {
            throw new JdbcProxyException("Could not find proxy constructor in JDK generated DatabaseMetaData proxy class", e);
         }
      }

      private Class locateDatabaseMetaDataProxyClass() {
         return Proxy.getProxyClass(JdbcWrapper.class.getClassLoader(), ProxyBuilder.METADATA_PROXY_INTERFACES);
      }
   });

   public static Connection buildConnection(LogicalConnectionImplementor logicalConnection) {
      ConnectionProxyHandler proxyHandler = new ConnectionProxyHandler(logicalConnection);

      try {
         return (Connection)((Constructor)connectionProxyConstructorValue.getValue()).newInstance(proxyHandler);
      } catch (Exception e) {
         throw new JdbcProxyException("Could not instantiate JDBC Connection proxy", e);
      }
   }

   public static Statement buildStatement(Statement statement, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      BasicStatementProxyHandler proxyHandler = new BasicStatementProxyHandler(statement, connectionProxyHandler, connectionProxy);

      try {
         return (Statement)((Constructor)statementProxyConstructorValue.getValue()).newInstance(proxyHandler);
      } catch (Exception e) {
         throw new JdbcProxyException("Could not instantiate JDBC Statement proxy", e);
      }
   }

   public static Statement buildImplicitStatement(Statement statement, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      if (statement == null) {
         return null;
      } else {
         ImplicitStatementProxyHandler proxyHandler = new ImplicitStatementProxyHandler(statement, connectionProxyHandler, connectionProxy);

         try {
            return (Statement)((Constructor)statementProxyConstructorValue.getValue()).newInstance(proxyHandler);
         } catch (Exception e) {
            throw new JdbcProxyException("Could not instantiate JDBC Statement proxy", e);
         }
      }
   }

   public static PreparedStatement buildPreparedStatement(String sql, Statement statement, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      PreparedStatementProxyHandler proxyHandler = new PreparedStatementProxyHandler(sql, statement, connectionProxyHandler, connectionProxy);

      try {
         return (PreparedStatement)((Constructor)preparedStatementProxyConstructorValue.getValue()).newInstance(proxyHandler);
      } catch (Exception e) {
         throw new JdbcProxyException("Could not instantiate JDBC PreparedStatement proxy", e);
      }
   }

   public static CallableStatement buildCallableStatement(String sql, CallableStatement statement, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      CallableStatementProxyHandler proxyHandler = new CallableStatementProxyHandler(sql, statement, connectionProxyHandler, connectionProxy);

      try {
         return (CallableStatement)((Constructor)callableStatementProxyConstructorValue.getValue()).newInstance(proxyHandler);
      } catch (Exception e) {
         throw new JdbcProxyException("Could not instantiate JDBC CallableStatement proxy", e);
      }
   }

   public static ResultSet buildResultSet(ResultSet resultSet, AbstractStatementProxyHandler statementProxyHandler, Statement statementProxy) {
      ResultSetProxyHandler proxyHandler = new ResultSetProxyHandler(resultSet, statementProxyHandler, statementProxy);

      try {
         return (ResultSet)((Constructor)resultSetProxyConstructorValue.getValue()).newInstance(proxyHandler);
      } catch (Exception e) {
         throw new JdbcProxyException("Could not instantiate JDBC ResultSet proxy", e);
      }
   }

   public static ResultSet buildImplicitResultSet(ResultSet resultSet, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      ImplicitResultSetProxyHandler proxyHandler = new ImplicitResultSetProxyHandler(resultSet, connectionProxyHandler, connectionProxy);

      try {
         return (ResultSet)((Constructor)resultSetProxyConstructorValue.getValue()).newInstance(proxyHandler);
      } catch (Exception e) {
         throw new JdbcProxyException("Could not instantiate JDBC ResultSet proxy", e);
      }
   }

   public static ResultSet buildImplicitResultSet(ResultSet resultSet, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy, Statement sourceStatement) {
      ImplicitResultSetProxyHandler proxyHandler = new ImplicitResultSetProxyHandler(resultSet, connectionProxyHandler, connectionProxy, sourceStatement);

      try {
         return (ResultSet)((Constructor)resultSetProxyConstructorValue.getValue()).newInstance(proxyHandler);
      } catch (Exception e) {
         throw new JdbcProxyException("Could not instantiate JDBC ResultSet proxy", e);
      }
   }

   public static DatabaseMetaData buildDatabaseMetaData(DatabaseMetaData metaData, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      DatabaseMetaDataProxyHandler proxyHandler = new DatabaseMetaDataProxyHandler(metaData, connectionProxyHandler, connectionProxy);

      try {
         return (DatabaseMetaData)((Constructor)metadataProxyConstructorValue.getValue()).newInstance(proxyHandler);
      } catch (Exception e) {
         throw new JdbcProxyException("Could not instantiate JDBC DatabaseMetaData proxy", e);
      }
   }
}
