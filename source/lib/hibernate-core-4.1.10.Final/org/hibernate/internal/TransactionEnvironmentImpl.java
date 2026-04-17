package org.hibernate.internal;

import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.spi.TransactionEnvironment;
import org.hibernate.engine.transaction.spi.TransactionFactory;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.stat.spi.StatisticsImplementor;

public class TransactionEnvironmentImpl implements TransactionEnvironment {
   private final SessionFactoryImpl sessionFactory;
   private final transient StatisticsImplementor statisticsImplementor;
   private final transient ServiceRegistry serviceRegistry;
   private final transient JdbcServices jdbcServices;
   private final transient JtaPlatform jtaPlatform;
   private final transient TransactionFactory transactionFactory;

   public TransactionEnvironmentImpl(SessionFactoryImpl sessionFactory) {
      this.sessionFactory = sessionFactory;
      this.statisticsImplementor = sessionFactory.getStatisticsImplementor();
      this.serviceRegistry = sessionFactory.getServiceRegistry();
      this.jdbcServices = (JdbcServices)this.serviceRegistry.getService(JdbcServices.class);
      this.jtaPlatform = (JtaPlatform)this.serviceRegistry.getService(JtaPlatform.class);
      this.transactionFactory = (TransactionFactory)this.serviceRegistry.getService(TransactionFactory.class);
   }

   public SessionFactoryImplementor getSessionFactory() {
      return this.sessionFactory;
   }

   protected ServiceRegistry serviceRegistry() {
      return this.serviceRegistry;
   }

   public JdbcServices getJdbcServices() {
      return this.jdbcServices;
   }

   public JtaPlatform getJtaPlatform() {
      return this.jtaPlatform;
   }

   public TransactionFactory getTransactionFactory() {
      return this.transactionFactory;
   }

   public StatisticsImplementor getStatisticsImplementor() {
      return this.statisticsImplementor;
   }
}
