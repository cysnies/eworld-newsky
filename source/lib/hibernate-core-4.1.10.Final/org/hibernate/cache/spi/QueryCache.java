package org.hibernate.cache.spi;

import java.util.List;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.cache.CacheException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.Type;

public interface QueryCache {
   void clear() throws CacheException;

   boolean put(QueryKey var1, Type[] var2, List var3, boolean var4, SessionImplementor var5) throws HibernateException;

   List get(QueryKey var1, Type[] var2, boolean var3, Set var4, SessionImplementor var5) throws HibernateException;

   void destroy();

   QueryResultsRegion getRegion();
}
