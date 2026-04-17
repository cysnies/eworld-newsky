package org.hibernate.stat;

import java.io.Serializable;

public interface QueryStatistics extends Serializable {
   long getExecutionCount();

   long getCacheHitCount();

   long getCachePutCount();

   long getCacheMissCount();

   long getExecutionRowCount();

   long getExecutionAvgTime();

   long getExecutionMaxTime();

   long getExecutionMinTime();
}
