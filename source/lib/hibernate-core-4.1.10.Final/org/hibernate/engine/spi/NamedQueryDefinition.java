package org.hibernate.engine.spi;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;

public class NamedQueryDefinition implements Serializable {
   private final String name;
   private final String query;
   private final boolean cacheable;
   private final String cacheRegion;
   private final Integer timeout;
   private final Integer lockTimeout;
   private final Integer fetchSize;
   private final FlushMode flushMode;
   private final Map parameterTypes;
   private CacheMode cacheMode;
   private boolean readOnly;
   private String comment;

   public NamedQueryDefinition(String query, boolean cacheable, String cacheRegion, Integer timeout, Integer fetchSize, FlushMode flushMode, Map parameterTypes) {
      this((String)null, query, cacheable, cacheRegion, timeout, fetchSize, flushMode, (CacheMode)null, false, (String)null, parameterTypes);
   }

   public NamedQueryDefinition(String name, String query, boolean cacheable, String cacheRegion, Integer timeout, Integer fetchSize, FlushMode flushMode, CacheMode cacheMode, boolean readOnly, String comment, Map parameterTypes) {
      this(name, query, cacheable, cacheRegion, timeout, -1, fetchSize, flushMode, cacheMode, readOnly, comment, parameterTypes);
   }

   public NamedQueryDefinition(String name, String query, boolean cacheable, String cacheRegion, Integer timeout, Integer lockTimeout, Integer fetchSize, FlushMode flushMode, CacheMode cacheMode, boolean readOnly, String comment, Map parameterTypes) {
      this.name = name;
      this.query = query;
      this.cacheable = cacheable;
      this.cacheRegion = cacheRegion;
      this.timeout = timeout;
      this.lockTimeout = lockTimeout;
      this.fetchSize = fetchSize;
      this.flushMode = flushMode;
      this.parameterTypes = parameterTypes;
      this.cacheMode = cacheMode;
      this.readOnly = readOnly;
      this.comment = comment;
   }

   public String getName() {
      return this.name;
   }

   public String getQueryString() {
      return this.query;
   }

   public boolean isCacheable() {
      return this.cacheable;
   }

   public String getCacheRegion() {
      return this.cacheRegion;
   }

   public Integer getFetchSize() {
      return this.fetchSize;
   }

   public Integer getTimeout() {
      return this.timeout;
   }

   public FlushMode getFlushMode() {
      return this.flushMode;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.query + ')';
   }

   public Map getParameterTypes() {
      return this.parameterTypes;
   }

   public String getQuery() {
      return this.query;
   }

   public CacheMode getCacheMode() {
      return this.cacheMode;
   }

   public boolean isReadOnly() {
      return this.readOnly;
   }

   public String getComment() {
      return this.comment;
   }

   public Integer getLockTimeout() {
      return this.lockTimeout;
   }
}
