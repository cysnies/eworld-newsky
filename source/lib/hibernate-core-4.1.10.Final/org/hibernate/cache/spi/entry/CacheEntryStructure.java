package org.hibernate.cache.spi.entry;

import org.hibernate.engine.spi.SessionFactoryImplementor;

public interface CacheEntryStructure {
   Object structure(Object var1);

   Object destructure(Object var1, SessionFactoryImplementor var2);
}
