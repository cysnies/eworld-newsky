package org.hibernate.service.jdbc.dialect.internal;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.hibernate.JDBCException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.resolver.BasicSQLExceptionConverter;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.jdbc.dialect.spi.DialectResolver;
import org.jboss.logging.Logger;

public abstract class AbstractDialectResolver implements DialectResolver {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractDialectResolver.class.getName());

   public final Dialect resolveDialect(DatabaseMetaData metaData) {
      try {
         return this.resolveDialectInternal(metaData);
      } catch (SQLException sqlException) {
         JDBCException jdbcException = BasicSQLExceptionConverter.INSTANCE.convert(sqlException);
         if (jdbcException instanceof JDBCConnectionException) {
            throw jdbcException;
         } else {
            LOG.warnf("%s : %s", BasicSQLExceptionConverter.MSG, sqlException.getMessage());
            return null;
         }
      } catch (Throwable t) {
         LOG.unableToExecuteResolver(this, t.getMessage());
         return null;
      }
   }

   protected abstract Dialect resolveDialectInternal(DatabaseMetaData var1) throws SQLException;
}
