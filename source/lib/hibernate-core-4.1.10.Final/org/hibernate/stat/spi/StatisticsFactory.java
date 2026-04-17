package org.hibernate.stat.spi;

import org.hibernate.engine.spi.SessionFactoryImplementor;

public interface StatisticsFactory {
   StatisticsImplementor buildStatistics(SessionFactoryImplementor var1);
}
