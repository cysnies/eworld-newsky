package org.hibernate.stat;

import java.io.Serializable;

public interface CollectionStatistics extends Serializable {
   long getLoadCount();

   long getFetchCount();

   long getRecreateCount();

   long getRemoveCount();

   long getUpdateCount();
}
