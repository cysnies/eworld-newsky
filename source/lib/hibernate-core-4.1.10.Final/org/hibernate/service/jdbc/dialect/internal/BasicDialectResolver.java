package org.hibernate.service.jdbc.dialect.internal;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;

public class BasicDialectResolver extends AbstractDialectResolver {
   public static final int VERSION_INSENSITIVE_VERSION = -9999;
   private final String matchingName;
   private final int matchingVersion;
   private final Class dialectClass;

   public BasicDialectResolver(String matchingName, Class dialectClass) {
      this(matchingName, -9999, dialectClass);
   }

   public BasicDialectResolver(String matchingName, int matchingVersion, Class dialectClass) {
      this.matchingName = matchingName;
      this.matchingVersion = matchingVersion;
      this.dialectClass = dialectClass;
   }

   protected final Dialect resolveDialectInternal(DatabaseMetaData metaData) throws SQLException {
      String databaseName = metaData.getDatabaseProductName();
      int databaseMajorVersion = metaData.getDatabaseMajorVersion();
      if (this.matchingName.equalsIgnoreCase(databaseName) && (this.matchingVersion == -9999 || this.matchingVersion == databaseMajorVersion)) {
         try {
            return (Dialect)this.dialectClass.newInstance();
         } catch (HibernateException e) {
            throw e;
         } catch (Throwable t) {
            throw new HibernateException("Could not instantiate specified Dialect class [" + this.dialectClass.getName() + "]", t);
         }
      } else {
         return null;
      }
   }
}
