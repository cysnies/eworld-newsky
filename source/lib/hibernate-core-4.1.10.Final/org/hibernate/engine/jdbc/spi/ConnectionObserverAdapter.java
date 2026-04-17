package org.hibernate.engine.jdbc.spi;

import java.sql.Connection;

public class ConnectionObserverAdapter implements ConnectionObserver {
   public void physicalConnectionObtained(Connection connection) {
   }

   public void physicalConnectionReleased() {
   }

   public void logicalConnectionClosed() {
   }

   public void statementPrepared() {
   }
}
