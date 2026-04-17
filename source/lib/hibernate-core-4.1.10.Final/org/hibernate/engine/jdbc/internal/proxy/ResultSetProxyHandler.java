package org.hibernate.engine.jdbc.internal.proxy;

import java.sql.ResultSet;
import java.sql.Statement;
import org.hibernate.engine.jdbc.spi.JdbcResourceRegistry;
import org.hibernate.engine.jdbc.spi.JdbcServices;

public class ResultSetProxyHandler extends AbstractResultSetProxyHandler {
   private AbstractStatementProxyHandler statementProxyHandler;
   private Statement statementProxy;

   public ResultSetProxyHandler(ResultSet resultSet, AbstractStatementProxyHandler statementProxyHandler, Statement statementProxy) {
      super(resultSet);
      this.statementProxyHandler = statementProxyHandler;
      this.statementProxy = statementProxy;
   }

   protected AbstractStatementProxyHandler getStatementProxy() {
      return this.statementProxyHandler;
   }

   protected Statement getExposableStatement() {
      return this.statementProxy;
   }

   protected JdbcServices getJdbcServices() {
      return this.getStatementProxy().getJdbcServices();
   }

   protected JdbcResourceRegistry getResourceRegistry() {
      return this.getStatementProxy().getResourceRegistry();
   }

   protected void invalidateHandle() {
      this.statementProxyHandler = null;
      super.invalidateHandle();
   }
}
