package org.hibernate.jmx;

import org.hibernate.stat.Statistics;

/** @deprecated */
@Deprecated
public interface StatisticsServiceMBean extends Statistics {
   void setSessionFactoryJNDIName(String var1);
}
