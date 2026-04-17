package org.hibernate.stat;

import java.io.Serializable;
import java.util.Map;

public interface NaturalIdCacheStatistics extends Serializable {
   long getHitCount();

   long getMissCount();

   long getPutCount();

   long getExecutionCount();

   long getExecutionAvgTime();

   long getExecutionMaxTime();

   long getExecutionMinTime();

   long getElementCountInMemory();

   long getElementCountOnDisk();

   long getSizeInMemory();

   Map getEntries();
}
