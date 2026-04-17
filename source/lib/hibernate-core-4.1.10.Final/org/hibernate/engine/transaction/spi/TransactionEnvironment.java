package org.hibernate.engine.transaction.spi;

import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.stat.spi.StatisticsImplementor;

public interface TransactionEnvironment {
   SessionFactoryImplementor getSessionFactory();

   JdbcServices getJdbcServices();

   JtaPlatform getJtaPlatform();

   TransactionFactory getTransactionFactory();

   StatisticsImplementor getStatisticsImplementor();
}
