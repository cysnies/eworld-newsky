package org.hibernate.stat;

import java.io.Serializable;

public interface EntityStatistics extends Serializable {
   long getDeleteCount();

   long getInsertCount();

   long getLoadCount();

   long getUpdateCount();

   long getFetchCount();

   long getOptimisticFailureCount();
}
