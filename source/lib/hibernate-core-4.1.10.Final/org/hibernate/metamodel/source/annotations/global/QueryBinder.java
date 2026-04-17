package org.hibernate.metamodel.source.annotations.global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryRootReturn;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.logging.Logger;

public class QueryBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, QueryBinder.class.getName());

   private QueryBinder() {
   }

   public static void bind(AnnotationBindingContext bindingContext) {
      for(AnnotationInstance query : bindingContext.getIndex().getAnnotations(JPADotNames.NAMED_QUERY)) {
         bindNamedQuery(bindingContext.getMetadataImplementor(), query);
      }

      for(AnnotationInstance queries : bindingContext.getIndex().getAnnotations(JPADotNames.NAMED_QUERIES)) {
         for(AnnotationInstance query : (AnnotationInstance[])JandexHelper.getValue(queries, "value", AnnotationInstance[].class)) {
            bindNamedQuery(bindingContext.getMetadataImplementor(), query);
         }
      }

      for(AnnotationInstance query : bindingContext.getIndex().getAnnotations(JPADotNames.NAMED_NATIVE_QUERY)) {
         bindNamedNativeQuery(bindingContext.getMetadataImplementor(), query);
      }

      for(AnnotationInstance queries : bindingContext.getIndex().getAnnotations(JPADotNames.NAMED_NATIVE_QUERIES)) {
         for(AnnotationInstance query : (AnnotationInstance[])JandexHelper.getValue(queries, "value", AnnotationInstance[].class)) {
            bindNamedNativeQuery(bindingContext.getMetadataImplementor(), query);
         }
      }

      for(AnnotationInstance query : bindingContext.getIndex().getAnnotations(HibernateDotNames.NAMED_QUERY)) {
         bindNamedQuery(bindingContext.getMetadataImplementor(), query);
      }

      for(AnnotationInstance queries : bindingContext.getIndex().getAnnotations(HibernateDotNames.NAMED_QUERIES)) {
         for(AnnotationInstance query : (AnnotationInstance[])JandexHelper.getValue(queries, "value", AnnotationInstance[].class)) {
            bindNamedQuery(bindingContext.getMetadataImplementor(), query);
         }
      }

      for(AnnotationInstance query : bindingContext.getIndex().getAnnotations(HibernateDotNames.NAMED_NATIVE_QUERY)) {
         bindNamedNativeQuery(bindingContext.getMetadataImplementor(), query);
      }

      for(AnnotationInstance queries : bindingContext.getIndex().getAnnotations(HibernateDotNames.NAMED_NATIVE_QUERIES)) {
         for(AnnotationInstance query : (AnnotationInstance[])JandexHelper.getValue(queries, "value", AnnotationInstance[].class)) {
            bindNamedNativeQuery(bindingContext.getMetadataImplementor(), query);
         }
      }

   }

   private static void bindNamedQuery(MetadataImplementor metadata, AnnotationInstance annotation) {
      String name = (String)JandexHelper.getValue(annotation, "name", String.class);
      if (StringHelper.isEmpty(name)) {
         throw new AnnotationException("A named query must have a name when used in class or package level");
      } else {
         String query = (String)JandexHelper.getValue(annotation, "query", String.class);
         AnnotationInstance[] hints = (AnnotationInstance[])JandexHelper.getValue(annotation, "hints", AnnotationInstance[].class);
         String cacheRegion = getString(hints, "org.hibernate.cacheRegion");
         if (StringHelper.isEmpty(cacheRegion)) {
            cacheRegion = null;
         }

         Integer timeout = getTimeout(hints, query);
         if (timeout != null && timeout < 0) {
            timeout = null;
         }

         Integer fetchSize = getInteger(hints, "org.hibernate.fetchSize", name);
         if (fetchSize != null && fetchSize < 0) {
            fetchSize = null;
         }

         String comment = getString(hints, "org.hibernate.comment");
         if (StringHelper.isEmpty(comment)) {
            comment = null;
         }

         metadata.addNamedQuery(new NamedQueryDefinition(name, query, getBoolean(hints, "org.hibernate.cacheable", name), cacheRegion, timeout, fetchSize, getFlushMode(hints, "org.hibernate.flushMode", name), getCacheMode(hints, "org.hibernate.cacheMode", name), getBoolean(hints, "org.hibernate.readOnly", name), comment, (Map)null));
         LOG.debugf("Binding named query: %s => %s", name, query);
      }
   }

   private static void bindNamedNativeQuery(MetadataImplementor metadata, AnnotationInstance annotation) {
      String name = (String)JandexHelper.getValue(annotation, "name", String.class);
      if (StringHelper.isEmpty(name)) {
         throw new AnnotationException("A named native query must have a name when used in class or package level");
      } else {
         String query = (String)JandexHelper.getValue(annotation, "query", String.class);
         String resultSetMapping = (String)JandexHelper.getValue(annotation, "resultSetMapping", String.class);
         AnnotationInstance[] hints = (AnnotationInstance[])JandexHelper.getValue(annotation, "hints", AnnotationInstance[].class);
         boolean cacheable = getBoolean(hints, "org.hibernate.cacheable", name);
         String cacheRegion = getString(hints, "org.hibernate.cacheRegion");
         if (StringHelper.isEmpty(cacheRegion)) {
            cacheRegion = null;
         }

         Integer timeout = getTimeout(hints, query);
         if (timeout != null && timeout < 0) {
            timeout = null;
         }

         Integer fetchSize = getInteger(hints, "org.hibernate.fetchSize", name);
         if (fetchSize != null && fetchSize < 0) {
            fetchSize = null;
         }

         FlushMode flushMode = getFlushMode(hints, "org.hibernate.flushMode", name);
         CacheMode cacheMode = getCacheMode(hints, "org.hibernate.cacheMode", name);
         boolean readOnly = getBoolean(hints, "org.hibernate.readOnly", name);
         String comment = getString(hints, "org.hibernate.comment");
         if (StringHelper.isEmpty(comment)) {
            comment = null;
         }

         boolean callable = getBoolean(hints, "org.hibernate.callable", name);
         NamedSQLQueryDefinition def;
         if (StringHelper.isNotEmpty(resultSetMapping)) {
            def = new NamedSQLQueryDefinition(name, query, resultSetMapping, (List)null, cacheable, cacheRegion, timeout, fetchSize, flushMode, cacheMode, readOnly, comment, (Map)null, callable);
         } else {
            AnnotationValue annotationValue = annotation.value("resultClass");
            if (annotationValue == null) {
               throw new NotYetImplementedException("Pure native scalar queries are not yet supported");
            }

            NativeSQLQueryRootReturn[] queryRoots = new NativeSQLQueryRootReturn[]{new NativeSQLQueryRootReturn("alias1", annotationValue.asString(), new HashMap(), LockMode.READ)};
            def = new NamedSQLQueryDefinition(name, query, queryRoots, (List)null, cacheable, cacheRegion, timeout, fetchSize, flushMode, cacheMode, readOnly, comment, (Map)null, callable);
         }

         metadata.addNamedNativeQuery(def);
         LOG.debugf("Binding named native query: %s => %s", name, query);
      }
   }

   private static boolean getBoolean(AnnotationInstance[] hints, String element, String query) {
      String val = getString(hints, element);
      if (val != null && !val.equalsIgnoreCase("false")) {
         if (val.equalsIgnoreCase("true")) {
            return true;
         } else {
            throw new AnnotationException("Not a boolean in hint: " + query + ":" + element);
         }
      } else {
         return false;
      }
   }

   private static CacheMode getCacheMode(AnnotationInstance[] hints, String element, String query) {
      String val = getString(hints, element);
      if (val == null) {
         return null;
      } else if (val.equalsIgnoreCase(CacheMode.GET.toString())) {
         return CacheMode.GET;
      } else if (val.equalsIgnoreCase(CacheMode.IGNORE.toString())) {
         return CacheMode.IGNORE;
      } else if (val.equalsIgnoreCase(CacheMode.NORMAL.toString())) {
         return CacheMode.NORMAL;
      } else if (val.equalsIgnoreCase(CacheMode.PUT.toString())) {
         return CacheMode.PUT;
      } else if (val.equalsIgnoreCase(CacheMode.REFRESH.toString())) {
         return CacheMode.REFRESH;
      } else {
         throw new AnnotationException("Unknown CacheMode in hint: " + query + ":" + element);
      }
   }

   private static FlushMode getFlushMode(AnnotationInstance[] hints, String element, String query) {
      String val = getString(hints, element);
      if (val == null) {
         return null;
      } else if (val.equalsIgnoreCase(FlushMode.ALWAYS.toString())) {
         return FlushMode.ALWAYS;
      } else if (val.equalsIgnoreCase(FlushMode.AUTO.toString())) {
         return FlushMode.AUTO;
      } else if (val.equalsIgnoreCase(FlushMode.COMMIT.toString())) {
         return FlushMode.COMMIT;
      } else if (val.equalsIgnoreCase(FlushMode.NEVER.toString())) {
         return FlushMode.MANUAL;
      } else if (val.equalsIgnoreCase(FlushMode.MANUAL.toString())) {
         return FlushMode.MANUAL;
      } else {
         throw new AnnotationException("Unknown FlushMode in hint: " + query + ":" + element);
      }
   }

   private static Integer getInteger(AnnotationInstance[] hints, String element, String query) {
      String val = getString(hints, element);
      if (val == null) {
         return null;
      } else {
         try {
            return Integer.decode(val);
         } catch (NumberFormatException nfe) {
            throw new AnnotationException("Not an integer in hint: " + query + ":" + element, nfe);
         }
      }
   }

   private static String getString(AnnotationInstance[] hints, String element) {
      for(AnnotationInstance hint : hints) {
         if (element.equals(JandexHelper.getValue(hint, "name", String.class))) {
            return (String)JandexHelper.getValue(hint, "value", String.class);
         }
      }

      return null;
   }

   private static Integer getTimeout(AnnotationInstance[] hints, String query) {
      Integer timeout = getInteger(hints, "javax.persistence.query.timeout", query);
      return timeout == null ? getInteger(hints, "org.hibernate.timeout", query) : (timeout + 500) / 1000;
   }
}
