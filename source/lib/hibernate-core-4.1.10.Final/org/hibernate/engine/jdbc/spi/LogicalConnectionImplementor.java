package org.hibernate.engine.jdbc.spi;

import java.sql.Connection;
import org.hibernate.ConnectionReleaseMode;

public interface LogicalConnectionImplementor extends LogicalConnection {
   JdbcServices getJdbcServices();

   JdbcResourceRegistry getResourceRegistry();

   void addObserver(ConnectionObserver var1);

   void removeObserver(ConnectionObserver var1);

   ConnectionReleaseMode getConnectionReleaseMode();

   void afterStatementExecution();

   void afterTransaction();

   void disableReleases();

   void enableReleases();

   Connection manualDisconnect();

   void manualReconnect(Connection var1);

   boolean isAutoCommit();

   boolean isReadyForSerialization();

   void notifyObserversStatementPrepared();
}
