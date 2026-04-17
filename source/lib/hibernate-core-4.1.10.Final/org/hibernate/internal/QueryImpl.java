package org.hibernate.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.query.spi.ParameterMetadata;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;

public class QueryImpl extends AbstractQueryImpl {
   private LockOptions lockOptions;

   public QueryImpl(String queryString, FlushMode flushMode, SessionImplementor session, ParameterMetadata parameterMetadata) {
      super(queryString, flushMode, session, parameterMetadata);
      this.lockOptions = new LockOptions();
   }

   public QueryImpl(String queryString, SessionImplementor session, ParameterMetadata parameterMetadata) {
      this(queryString, (FlushMode)null, session, parameterMetadata);
   }

   public Iterator iterate() throws HibernateException {
      this.verifyParameters();
      Map namedParams = this.getNamedParams();
      this.before();

      Iterator var2;
      try {
         var2 = this.getSession().iterate(this.expandParameterLists(namedParams), this.getQueryParameters(namedParams));
      } finally {
         this.after();
      }

      return var2;
   }

   public ScrollableResults scroll() throws HibernateException {
      return this.scroll(ScrollMode.SCROLL_INSENSITIVE);
   }

   public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
      this.verifyParameters();
      Map namedParams = this.getNamedParams();
      this.before();
      QueryParameters qp = this.getQueryParameters(namedParams);
      qp.setScrollMode(scrollMode);

      ScrollableResults var4;
      try {
         var4 = this.getSession().scroll(this.expandParameterLists(namedParams), qp);
      } finally {
         this.after();
      }

      return var4;
   }

   public List list() throws HibernateException {
      this.verifyParameters();
      Map namedParams = this.getNamedParams();
      this.before();

      List var2;
      try {
         var2 = this.getSession().list(this.expandParameterLists(namedParams), this.getQueryParameters(namedParams));
      } finally {
         this.after();
      }

      return var2;
   }

   public int executeUpdate() throws HibernateException {
      this.verifyParameters();
      Map namedParams = this.getNamedParams();
      this.before();

      int var2;
      try {
         var2 = this.getSession().executeUpdate(this.expandParameterLists(namedParams), this.getQueryParameters(namedParams));
      } finally {
         this.after();
      }

      return var2;
   }

   public Query setLockMode(String alias, LockMode lockMode) {
      this.lockOptions.setAliasSpecificLockMode(alias, lockMode);
      return this;
   }

   public Query setLockOptions(LockOptions lockOption) {
      this.lockOptions.setLockMode(lockOption.getLockMode());
      this.lockOptions.setScope(lockOption.getScope());
      this.lockOptions.setTimeOut(lockOption.getTimeOut());
      return this;
   }

   public LockOptions getLockOptions() {
      return this.lockOptions;
   }
}
