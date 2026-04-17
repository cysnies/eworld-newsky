package org.hibernate.service.jdbc.dialect.internal;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hibernate.dialect.Dialect;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.jdbc.dialect.spi.DialectResolver;
import org.jboss.logging.Logger;

public class DialectResolverSet implements DialectResolver {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DialectResolverSet.class.getName());
   private List resolvers;

   public DialectResolverSet() {
      this((List)(new ArrayList()));
   }

   public DialectResolverSet(List resolvers) {
      this.resolvers = resolvers;
   }

   public DialectResolverSet(DialectResolver... resolvers) {
      this(Arrays.asList(resolvers));
   }

   public Dialect resolveDialect(DatabaseMetaData metaData) throws JDBCConnectionException {
      for(DialectResolver resolver : this.resolvers) {
         try {
            Dialect dialect = resolver.resolveDialect(metaData);
            if (dialect != null) {
               return dialect;
            }
         } catch (JDBCConnectionException e) {
            throw e;
         } catch (Exception e) {
            LOG.exceptionInSubResolver(e.getMessage());
         }
      }

      return null;
   }

   public void addResolver(DialectResolver resolver) {
      this.resolvers.add(resolver);
   }

   public void addResolverAtFirst(DialectResolver resolver) {
      this.resolvers.add(0, resolver);
   }
}
