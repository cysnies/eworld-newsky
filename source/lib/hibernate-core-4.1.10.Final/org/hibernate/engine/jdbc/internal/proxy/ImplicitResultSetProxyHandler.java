package org.hibernate.engine.jdbc.internal.proxy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.hibernate.engine.jdbc.spi.JdbcResourceRegistry;
import org.hibernate.engine.jdbc.spi.JdbcServices;

public class ImplicitResultSetProxyHandler extends AbstractResultSetProxyHandler {
   private ConnectionProxyHandler connectionProxyHandler;
   private Connection connectionProxy;
   private Statement sourceStatement;

   public ImplicitResultSetProxyHandler(ResultSet resultSet, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      super(resultSet);
      this.connectionProxyHandler = connectionProxyHandler;
      this.connectionProxy = connectionProxy;
   }

   public ImplicitResultSetProxyHandler(ResultSet resultSet, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy, Statement sourceStatement) {
      super(resultSet);
      this.connectionProxyHandler = connectionProxyHandler;
      this.connectionProxy = connectionProxy;
      this.sourceStatement = sourceStatement;
   }

   protected JdbcServices getJdbcServices() {
      return this.connectionProxyHandler.getJdbcServices();
   }

   protected JdbcResourceRegistry getResourceRegistry() {
      return this.connectionProxyHandler.getResourceRegistry();
   }

   protected Statement getExposableStatement() {
      if (this.sourceStatement == null) {
         try {
            Statement stmnt = this.getResultSet().getStatement();
            if (stmnt == null) {
               return null;
            }

            this.sourceStatement = ProxyBuilder.buildImplicitStatement(stmnt, this.connectionProxyHandler, this.connectionProxy);
         } catch (SQLException e) {
            throw this.getJdbcServices().getSqlExceptionHelper().convert(e, e.getMessage());
         }
      }

      return this.sourceStatement;
   }

   protected void invalidateHandle() {
      this.sourceStatement = null;
      super.invalidateHandle();
   }
}
