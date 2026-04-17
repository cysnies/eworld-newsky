package org.hibernate.stat;

import java.util.Set;

public interface SessionStatistics {
   int getEntityCount();

   int getCollectionCount();

   Set getEntityKeys();

   Set getCollectionKeys();
}
