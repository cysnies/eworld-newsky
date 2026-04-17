package org.hibernate;

import java.util.List;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.ResultTransformer;

public interface Criteria extends CriteriaSpecification {
   String getAlias();

   Criteria setProjection(Projection var1);

   Criteria add(Criterion var1);

   Criteria addOrder(Order var1);

   Criteria setFetchMode(String var1, FetchMode var2) throws HibernateException;

   Criteria setLockMode(LockMode var1);

   Criteria setLockMode(String var1, LockMode var2);

   Criteria createAlias(String var1, String var2) throws HibernateException;

   Criteria createAlias(String var1, String var2, JoinType var3) throws HibernateException;

   /** @deprecated */
   @Deprecated
   Criteria createAlias(String var1, String var2, int var3) throws HibernateException;

   Criteria createAlias(String var1, String var2, JoinType var3, Criterion var4) throws HibernateException;

   /** @deprecated */
   @Deprecated
   Criteria createAlias(String var1, String var2, int var3, Criterion var4) throws HibernateException;

   Criteria createCriteria(String var1) throws HibernateException;

   Criteria createCriteria(String var1, JoinType var2) throws HibernateException;

   /** @deprecated */
   @Deprecated
   Criteria createCriteria(String var1, int var2) throws HibernateException;

   Criteria createCriteria(String var1, String var2) throws HibernateException;

   Criteria createCriteria(String var1, String var2, JoinType var3) throws HibernateException;

   /** @deprecated */
   @Deprecated
   Criteria createCriteria(String var1, String var2, int var3) throws HibernateException;

   Criteria createCriteria(String var1, String var2, JoinType var3, Criterion var4) throws HibernateException;

   /** @deprecated */
   @Deprecated
   Criteria createCriteria(String var1, String var2, int var3, Criterion var4) throws HibernateException;

   Criteria setResultTransformer(ResultTransformer var1);

   Criteria setMaxResults(int var1);

   Criteria setFirstResult(int var1);

   boolean isReadOnlyInitialized();

   boolean isReadOnly();

   Criteria setReadOnly(boolean var1);

   Criteria setFetchSize(int var1);

   Criteria setTimeout(int var1);

   Criteria setCacheable(boolean var1);

   Criteria setCacheRegion(String var1);

   Criteria setComment(String var1);

   Criteria setFlushMode(FlushMode var1);

   Criteria setCacheMode(CacheMode var1);

   List list() throws HibernateException;

   ScrollableResults scroll() throws HibernateException;

   ScrollableResults scroll(ScrollMode var1) throws HibernateException;

   Object uniqueResult() throws HibernateException;
}
