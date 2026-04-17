package org.hibernate.engine.jdbc.spi;

import java.sql.Connection;

public interface ConnectionObserver {
   void physicalConnectionObtained(Connection var1);

   void physicalConnectionReleased();

   void logicalConnectionClosed();

   void statementPrepared();
}
