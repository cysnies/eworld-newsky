package org.hibernate.annotations;

public class QueryHints {
   public static final String CACHE_MODE = "org.hibernate.cacheMode";
   public static final String CACHE_REGION = "org.hibernate.cacheRegion";
   public static final String CACHEABLE = "org.hibernate.cacheable";
   public static final String CALLABLE = "org.hibernate.callable";
   public static final String COMMENT = "org.hibernate.comment";
   public static final String FETCH_SIZE = "org.hibernate.fetchSize";
   public static final String FLUSH_MODE = "org.hibernate.flushMode";
   public static final String READ_ONLY = "org.hibernate.readOnly";
   public static final String TIMEOUT_HIBERNATE = "org.hibernate.timeout";
   public static final String TIMEOUT_JPA = "javax.persistence.query.timeout";

   private QueryHints() {
   }
}
