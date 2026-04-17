package org.hibernate.engine.spi;

import java.util.List;
import java.util.Map;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryReturn;

public class NamedSQLQueryDefinition extends NamedQueryDefinition {
   private NativeSQLQueryReturn[] queryReturns;
   private final List querySpaces;
   private final boolean callable;
   private String resultSetRef;

   public NamedSQLQueryDefinition(String name, String query, NativeSQLQueryReturn[] queryReturns, List querySpaces, boolean cacheable, String cacheRegion, Integer timeout, Integer fetchSize, FlushMode flushMode, CacheMode cacheMode, boolean readOnly, String comment, Map parameterTypes, boolean callable) {
      super(name, query.trim(), cacheable, cacheRegion, timeout, fetchSize, flushMode, cacheMode, readOnly, comment, parameterTypes);
      this.queryReturns = queryReturns;
      this.querySpaces = querySpaces;
      this.callable = callable;
   }

   public NamedSQLQueryDefinition(String name, String query, String resultSetRef, List querySpaces, boolean cacheable, String cacheRegion, Integer timeout, Integer fetchSize, FlushMode flushMode, CacheMode cacheMode, boolean readOnly, String comment, Map parameterTypes, boolean callable) {
      super(name, query.trim(), cacheable, cacheRegion, timeout, fetchSize, flushMode, cacheMode, readOnly, comment, parameterTypes);
      this.resultSetRef = resultSetRef;
      this.querySpaces = querySpaces;
      this.callable = callable;
   }

   /** @deprecated */
   public NamedSQLQueryDefinition(String query, String resultSetRef, List querySpaces, boolean cacheable, String cacheRegion, Integer timeout, Integer fetchSize, FlushMode flushMode, Map parameterTypes, boolean callable) {
      this((String)null, query, (String)resultSetRef, querySpaces, cacheable, cacheRegion, timeout, fetchSize, flushMode, (CacheMode)null, false, (String)null, parameterTypes, callable);
   }

   public NativeSQLQueryReturn[] getQueryReturns() {
      return this.queryReturns;
   }

   public List getQuerySpaces() {
      return this.querySpaces;
   }

   public boolean isCallable() {
      return this.callable;
   }

   public String getResultSetRef() {
      return this.resultSetRef;
   }
}
