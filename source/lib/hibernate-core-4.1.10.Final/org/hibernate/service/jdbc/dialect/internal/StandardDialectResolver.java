package org.hibernate.service.jdbc.dialect.internal;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.hibernate.dialect.CUBRIDDialect;
import org.hibernate.dialect.DB2400Dialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.DerbyTenFiveDialect;
import org.hibernate.dialect.DerbyTenSevenDialect;
import org.hibernate.dialect.DerbyTenSixDialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.Ingres10Dialect;
import org.hibernate.dialect.Ingres9Dialect;
import org.hibernate.dialect.IngresDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.SQLServer2005Dialect;
import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseASE15Dialect;
import org.hibernate.dialect.SybaseAnywhereDialect;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class StandardDialectResolver extends AbstractDialectResolver {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, StandardDialectResolver.class.getName());

   protected Dialect resolveDialectInternal(DatabaseMetaData metaData) throws SQLException {
      String databaseName = metaData.getDatabaseProductName();
      int databaseMajorVersion = metaData.getDatabaseMajorVersion();
      if ("CUBRID".equalsIgnoreCase(databaseName)) {
         return new CUBRIDDialect();
      } else if ("HSQL Database Engine".equals(databaseName)) {
         return new HSQLDialect();
      } else if ("H2".equals(databaseName)) {
         return new H2Dialect();
      } else if ("MySQL".equals(databaseName)) {
         return new MySQLDialect();
      } else if ("PostgreSQL".equals(databaseName)) {
         int databaseMinorVersion = metaData.getDatabaseMinorVersion();
         return (Dialect)(databaseMajorVersion <= 8 && (databaseMajorVersion != 8 || databaseMinorVersion < 2) ? new PostgreSQL81Dialect() : new PostgreSQL82Dialect());
      } else if (!"Apache Derby".equals(databaseName)) {
         if ("ingres".equalsIgnoreCase(databaseName)) {
            switch (databaseMajorVersion) {
               case 9:
                  int databaseMinorVersion = metaData.getDatabaseMinorVersion();
                  if (databaseMinorVersion > 2) {
                     return new Ingres9Dialect();
                  }

                  return new IngresDialect();
               case 10:
                  return new Ingres10Dialect();
               default:
                  LOG.unknownIngresVersion(databaseMajorVersion);
                  return new IngresDialect();
            }
         } else if (databaseName.startsWith("Microsoft SQL Server")) {
            switch (databaseMajorVersion) {
               case 8:
                  return new SQLServerDialect();
               case 9:
                  return new SQLServer2005Dialect();
               case 10:
               case 11:
                  return new SQLServer2008Dialect();
               default:
                  LOG.unknownSqlServerVersion(databaseMajorVersion);
                  return new SQLServerDialect();
            }
         } else if (!"Sybase SQL Server".equals(databaseName) && !"Adaptive Server Enterprise".equals(databaseName)) {
            if (databaseName.startsWith("Adaptive Server Anywhere")) {
               return new SybaseAnywhereDialect();
            } else if ("Informix Dynamic Server".equals(databaseName)) {
               return new InformixDialect();
            } else if (databaseName.equals("DB2 UDB for AS/400")) {
               return new DB2400Dialect();
            } else if (databaseName.startsWith("DB2/")) {
               return new DB2Dialect();
            } else {
               if ("Oracle".equals(databaseName)) {
                  switch (databaseMajorVersion) {
                     case 8:
                        return new Oracle8iDialect();
                     case 9:
                        return new Oracle9iDialect();
                     case 10:
                        return new Oracle10gDialect();
                     case 11:
                        return new Oracle10gDialect();
                     default:
                        LOG.unknownOracleVersion(databaseMajorVersion);
                  }
               }

               return null;
            }
         } else {
            return new SybaseASE15Dialect();
         }
      } else {
         int databaseMinorVersion = metaData.getDatabaseMinorVersion();
         if (databaseMajorVersion <= 10 && (databaseMajorVersion != 10 || databaseMinorVersion < 7)) {
            if (databaseMajorVersion == 10 && databaseMinorVersion == 6) {
               return new DerbyTenSixDialect();
            } else {
               return (Dialect)(databaseMajorVersion == 10 && databaseMinorVersion == 5 ? new DerbyTenFiveDialect() : new DerbyDialect());
            }
         } else {
            return new DerbyTenSevenDialect();
         }
      }
   }
}
