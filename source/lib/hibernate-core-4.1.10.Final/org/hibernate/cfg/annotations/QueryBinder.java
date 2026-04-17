package org.hibernate.cfg.annotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import org.hibernate.AnnotationException;
import org.hibernate.AssertionFailure;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.annotations.CacheModeType;
import org.hibernate.annotations.FlushModeType;
import org.hibernate.cfg.BinderHelper;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryRootReturn;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public abstract class QueryBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, QueryBinder.class.getName());

   public static void bindQuery(NamedQuery queryAnn, Mappings mappings, boolean isDefault) {
      if (queryAnn != null) {
         if (BinderHelper.isEmptyAnnotationValue(queryAnn.name())) {
            throw new AnnotationException("A named query must have a name when used in class or package level");
         } else {
            QueryHint[] hints = queryAnn.hints();
            String queryName = queryAnn.query();
            NamedQueryDefinition query = new NamedQueryDefinition(queryAnn.name(), queryName, getBoolean(queryName, "org.hibernate.cacheable", hints), getString(queryName, "org.hibernate.cacheRegion", hints), getTimeout(queryName, hints), getInteger(queryName, "javax.persistence.lock.timeout", hints), getInteger(queryName, "org.hibernate.fetchSize", hints), getFlushMode(queryName, hints), getCacheMode(queryName, hints), getBoolean(queryName, "org.hibernate.readOnly", hints), getString(queryName, "org.hibernate.comment", hints), (Map)null);
            if (isDefault) {
               mappings.addDefaultQuery(query.getName(), query);
            } else {
               mappings.addQuery(query.getName(), query);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debugf("Binding named query: %s => %s", query.getName(), query.getQueryString());
            }

         }
      }
   }

   public static void bindNativeQuery(NamedNativeQuery queryAnn, Mappings mappings, boolean isDefault) {
      if (queryAnn != null) {
         if (BinderHelper.isEmptyAnnotationValue(queryAnn.name())) {
            throw new AnnotationException("A named query must have a name when used in class or package level");
         } else {
            String resultSetMapping = queryAnn.resultSetMapping();
            QueryHint[] hints = queryAnn.hints();
            String queryName = queryAnn.query();
            NamedSQLQueryDefinition query;
            if (!BinderHelper.isEmptyAnnotationValue(resultSetMapping)) {
               query = new NamedSQLQueryDefinition(queryAnn.name(), queryName, resultSetMapping, (List)null, getBoolean(queryName, "org.hibernate.cacheable", hints), getString(queryName, "org.hibernate.cacheRegion", hints), getTimeout(queryName, hints), getInteger(queryName, "org.hibernate.fetchSize", hints), getFlushMode(queryName, hints), getCacheMode(queryName, hints), getBoolean(queryName, "org.hibernate.readOnly", hints), getString(queryName, "org.hibernate.comment", hints), (Map)null, getBoolean(queryName, "org.hibernate.callable", hints));
            } else {
               if (Void.TYPE.equals(queryAnn.resultClass())) {
                  throw new NotYetImplementedException("Pure native scalar queries are not yet supported");
               }

               NativeSQLQueryRootReturn entityQueryReturn = new NativeSQLQueryRootReturn("alias1", queryAnn.resultClass().getName(), new HashMap(), LockMode.READ);
               query = new NamedSQLQueryDefinition(queryAnn.name(), queryName, new NativeSQLQueryReturn[]{entityQueryReturn}, (List)null, getBoolean(queryName, "org.hibernate.cacheable", hints), getString(queryName, "org.hibernate.cacheRegion", hints), getTimeout(queryName, hints), getInteger(queryName, "org.hibernate.fetchSize", hints), getFlushMode(queryName, hints), getCacheMode(queryName, hints), getBoolean(queryName, "org.hibernate.readOnly", hints), getString(queryName, "org.hibernate.comment", hints), (Map)null, getBoolean(queryName, "org.hibernate.callable", hints));
            }

            if (isDefault) {
               mappings.addDefaultSQLQuery(query.getName(), query);
            } else {
               mappings.addSQLQuery(query.getName(), query);
            }

            if (LOG.isDebugEnabled()) {
               LOG.debugf("Binding named native query: %s => %s", queryAnn.name(), queryAnn.query());
            }

         }
      }
   }

   public static void bindNativeQuery(org.hibernate.annotations.NamedNativeQuery queryAnn, Mappings mappings) {
      if (queryAnn != null) {
         if (BinderHelper.isEmptyAnnotationValue(queryAnn.name())) {
            throw new AnnotationException("A named query must have a name when used in class or package level");
         } else {
            String resultSetMapping = queryAnn.resultSetMapping();
            NamedSQLQueryDefinition query;
            if (!BinderHelper.isEmptyAnnotationValue(resultSetMapping)) {
               query = new NamedSQLQueryDefinition(queryAnn.name(), queryAnn.query(), resultSetMapping, (List)null, queryAnn.cacheable(), BinderHelper.isEmptyAnnotationValue(queryAnn.cacheRegion()) ? null : queryAnn.cacheRegion(), queryAnn.timeout() < 0 ? null : queryAnn.timeout(), queryAnn.fetchSize() < 0 ? null : queryAnn.fetchSize(), getFlushMode(queryAnn.flushMode()), getCacheMode(queryAnn.cacheMode()), queryAnn.readOnly(), BinderHelper.isEmptyAnnotationValue(queryAnn.comment()) ? null : queryAnn.comment(), (Map)null, queryAnn.callable());
            } else {
               if (Void.TYPE.equals(queryAnn.resultClass())) {
                  throw new NotYetImplementedException("Pure native scalar queries are not yet supported");
               }

               NativeSQLQueryRootReturn entityQueryReturn = new NativeSQLQueryRootReturn("alias1", queryAnn.resultClass().getName(), new HashMap(), LockMode.READ);
               query = new NamedSQLQueryDefinition(queryAnn.name(), queryAnn.query(), new NativeSQLQueryReturn[]{entityQueryReturn}, (List)null, queryAnn.cacheable(), BinderHelper.isEmptyAnnotationValue(queryAnn.cacheRegion()) ? null : queryAnn.cacheRegion(), queryAnn.timeout() < 0 ? null : queryAnn.timeout(), queryAnn.fetchSize() < 0 ? null : queryAnn.fetchSize(), getFlushMode(queryAnn.flushMode()), getCacheMode(queryAnn.cacheMode()), queryAnn.readOnly(), BinderHelper.isEmptyAnnotationValue(queryAnn.comment()) ? null : queryAnn.comment(), (Map)null, queryAnn.callable());
            }

            mappings.addSQLQuery(query.getName(), query);
            if (LOG.isDebugEnabled()) {
               LOG.debugf("Binding named native query: %s => %s", query.getName(), queryAnn.query());
            }

         }
      }
   }

   public static void bindQueries(NamedQueries queriesAnn, Mappings mappings, boolean isDefault) {
      if (queriesAnn != null) {
         for(NamedQuery q : queriesAnn.value()) {
            bindQuery(q, mappings, isDefault);
         }

      }
   }

   public static void bindNativeQueries(NamedNativeQueries queriesAnn, Mappings mappings, boolean isDefault) {
      if (queriesAnn != null) {
         for(NamedNativeQuery q : queriesAnn.value()) {
            bindNativeQuery(q, mappings, isDefault);
         }

      }
   }

   public static void bindNativeQueries(org.hibernate.annotations.NamedNativeQueries queriesAnn, Mappings mappings) {
      if (queriesAnn != null) {
         for(org.hibernate.annotations.NamedNativeQuery q : queriesAnn.value()) {
            bindNativeQuery(q, mappings);
         }

      }
   }

   public static void bindQuery(org.hibernate.annotations.NamedQuery queryAnn, Mappings mappings) {
      if (queryAnn != null) {
         if (BinderHelper.isEmptyAnnotationValue(queryAnn.name())) {
            throw new AnnotationException("A named query must have a name when used in class or package level");
         } else {
            FlushMode flushMode = getFlushMode(queryAnn.flushMode());
            NamedQueryDefinition query = new NamedQueryDefinition(queryAnn.name(), queryAnn.query(), queryAnn.cacheable(), BinderHelper.isEmptyAnnotationValue(queryAnn.cacheRegion()) ? null : queryAnn.cacheRegion(), queryAnn.timeout() < 0 ? null : queryAnn.timeout(), queryAnn.fetchSize() < 0 ? null : queryAnn.fetchSize(), flushMode, getCacheMode(queryAnn.cacheMode()), queryAnn.readOnly(), BinderHelper.isEmptyAnnotationValue(queryAnn.comment()) ? null : queryAnn.comment(), (Map)null);
            mappings.addQuery(query.getName(), query);
            if (LOG.isDebugEnabled()) {
               LOG.debugf("Binding named query: %s => %s", query.getName(), query.getQueryString());
            }

         }
      }
   }

   private static FlushMode getFlushMode(FlushModeType flushModeType) {
      FlushMode flushMode;
      switch (flushModeType) {
         case ALWAYS:
            flushMode = FlushMode.ALWAYS;
            break;
         case AUTO:
            flushMode = FlushMode.AUTO;
            break;
         case COMMIT:
            flushMode = FlushMode.COMMIT;
            break;
         case NEVER:
            flushMode = FlushMode.MANUAL;
            break;
         case MANUAL:
            flushMode = FlushMode.MANUAL;
            break;
         case PERSISTENCE_CONTEXT:
            flushMode = null;
            break;
         default:
            throw new AssertionFailure("Unknown flushModeType: " + flushModeType);
      }

      return flushMode;
   }

   private static CacheMode getCacheMode(CacheModeType cacheModeType) {
      switch (cacheModeType) {
         case GET:
            return CacheMode.GET;
         case IGNORE:
            return CacheMode.IGNORE;
         case NORMAL:
            return CacheMode.NORMAL;
         case PUT:
            return CacheMode.PUT;
         case REFRESH:
            return CacheMode.REFRESH;
         default:
            throw new AssertionFailure("Unknown cacheModeType: " + cacheModeType);
      }
   }

   public static void bindQueries(org.hibernate.annotations.NamedQueries queriesAnn, Mappings mappings) {
      if (queriesAnn != null) {
         for(org.hibernate.annotations.NamedQuery q : queriesAnn.value()) {
            bindQuery(q, mappings);
         }

      }
   }

   public static void bindSqlResultsetMappings(SqlResultSetMappings ann, Mappings mappings, boolean isDefault) {
      if (ann != null) {
         for(SqlResultSetMapping rs : ann.value()) {
            mappings.addSecondPass(new ResultsetMappingSecondPass(rs, mappings, true));
         }

      }
   }

   public static void bindSqlResultsetMapping(SqlResultSetMapping ann, Mappings mappings, boolean isDefault) {
      mappings.addSecondPass(new ResultsetMappingSecondPass(ann, mappings, isDefault));
   }

   private static CacheMode getCacheMode(String query, QueryHint[] hints) {
      for(QueryHint hint : hints) {
         if ("org.hibernate.cacheMode".equals(hint.name())) {
            if (hint.value().equalsIgnoreCase(CacheMode.GET.toString())) {
               return CacheMode.GET;
            }

            if (hint.value().equalsIgnoreCase(CacheMode.IGNORE.toString())) {
               return CacheMode.IGNORE;
            }

            if (hint.value().equalsIgnoreCase(CacheMode.NORMAL.toString())) {
               return CacheMode.NORMAL;
            }

            if (hint.value().equalsIgnoreCase(CacheMode.PUT.toString())) {
               return CacheMode.PUT;
            }

            if (hint.value().equalsIgnoreCase(CacheMode.REFRESH.toString())) {
               return CacheMode.REFRESH;
            }

            throw new AnnotationException("Unknown CacheMode in hint: " + query + ":" + hint.name());
         }
      }

      return null;
   }

   private static FlushMode getFlushMode(String query, QueryHint[] hints) {
      for(QueryHint hint : hints) {
         if ("org.hibernate.flushMode".equals(hint.name())) {
            if (hint.value().equalsIgnoreCase(FlushMode.ALWAYS.toString())) {
               return FlushMode.ALWAYS;
            }

            if (hint.value().equalsIgnoreCase(FlushMode.AUTO.toString())) {
               return FlushMode.AUTO;
            }

            if (hint.value().equalsIgnoreCase(FlushMode.COMMIT.toString())) {
               return FlushMode.COMMIT;
            }

            if (hint.value().equalsIgnoreCase(FlushMode.NEVER.toString())) {
               return FlushMode.MANUAL;
            }

            if (hint.value().equalsIgnoreCase(FlushMode.MANUAL.toString())) {
               return FlushMode.MANUAL;
            }

            throw new AnnotationException("Unknown FlushMode in hint: " + query + ":" + hint.name());
         }
      }

      return null;
   }

   private static boolean getBoolean(String query, String hintName, QueryHint[] hints) {
      for(QueryHint hint : hints) {
         if (hintName.equals(hint.name())) {
            if (hint.value().equalsIgnoreCase("true")) {
               return true;
            }

            if (hint.value().equalsIgnoreCase("false")) {
               return false;
            }

            throw new AnnotationException("Not a boolean in hint: " + query + ":" + hint.name());
         }
      }

      return false;
   }

   private static String getString(String query, String hintName, QueryHint[] hints) {
      for(QueryHint hint : hints) {
         if (hintName.equals(hint.name())) {
            return hint.value();
         }
      }

      return null;
   }

   private static Integer getInteger(String query, String hintName, QueryHint[] hints) {
      for(QueryHint hint : hints) {
         if (hintName.equals(hint.name())) {
            try {
               return Integer.decode(hint.value());
            } catch (NumberFormatException nfe) {
               throw new AnnotationException("Not an integer in hint: " + query + ":" + hint.name(), nfe);
            }
         }
      }

      return null;
   }

   private static Integer getTimeout(String queryName, QueryHint[] hints) {
      Integer timeout = getInteger(queryName, "javax.persistence.query.timeout", hints);
      if (timeout != null) {
         timeout = (int)Math.round(timeout.doubleValue() / (double)1000.0F);
      } else {
         timeout = getInteger(queryName, "org.hibernate.timeout", hints);
      }

      return timeout;
   }
}
