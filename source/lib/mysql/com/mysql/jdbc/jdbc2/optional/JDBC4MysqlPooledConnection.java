package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.StatementEvent;
import javax.sql.StatementEventListener;

public class JDBC4MysqlPooledConnection extends MysqlPooledConnection {
   private Map statementEventListeners = new HashMap();

   public JDBC4MysqlPooledConnection(Connection connection) {
      super(connection);
   }

   public synchronized void close() throws SQLException {
      super.close();
      if (this.statementEventListeners != null) {
         this.statementEventListeners.clear();
         this.statementEventListeners = null;
      }

   }

   public void addStatementEventListener(StatementEventListener listener) {
      synchronized(this.statementEventListeners) {
         this.statementEventListeners.put(listener, listener);
      }
   }

   public void removeStatementEventListener(StatementEventListener listener) {
      synchronized(this.statementEventListeners) {
         this.statementEventListeners.remove(listener);
      }
   }

   void fireStatementEvent(StatementEvent event) throws SQLException {
      synchronized(this.statementEventListeners) {
         for(StatementEventListener listener : this.statementEventListeners.keySet()) {
            listener.statementClosed(event);
         }

      }
   }
}
