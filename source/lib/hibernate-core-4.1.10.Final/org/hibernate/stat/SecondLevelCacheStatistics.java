package org.hibernate.stat;

import java.io.Serializable;
import java.util.Map;

public interface SecondLevelCacheStatistics extends Serializable {
   long getHitCount();

   long getMissCount();

   long getPutCount();

   long getElementCountInMemory();

   long getElementCountOnDisk();

   long getSizeInMemory();

   Map getEntries();
}
