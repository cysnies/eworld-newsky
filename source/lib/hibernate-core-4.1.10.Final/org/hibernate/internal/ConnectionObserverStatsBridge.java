package org.hibernate.internal;

import java.io.Serializable;
import java.sql.Connection;
import org.hibernate.engine.jdbc.spi.ConnectionObserver;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public class ConnectionObserverStatsBridge implements ConnectionObserver, Serializable {
   private final SessionFactoryImplementor sessionFactory;

   public ConnectionObserverStatsBridge(SessionFactoryImplementor sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   public void physicalConnectionObtained(Connection connection) {
      if (this.sessionFactory.getStatistics().isStatisticsEnabled()) {
         this.sessionFactory.getStatisticsImplementor().connect();
      }

   }

   public void physicalConnectionReleased() {
   }

   public void logicalConnectionClosed() {
   }

   public void statementPrepared() {
      if (this.sessionFactory.getStatistics().isStatisticsEnabled()) {
         this.sessionFactory.getStatisticsImplementor().prepareStatement();
      }

   }
}
