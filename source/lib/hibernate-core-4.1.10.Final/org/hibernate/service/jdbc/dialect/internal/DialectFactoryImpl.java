package org.hibernate.service.jdbc.dialect.internal;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;
import org.hibernate.service.jdbc.dialect.spi.DialectFactory;
import org.hibernate.service.jdbc.dialect.spi.DialectResolver;
import org.hibernate.service.spi.InjectService;

public class DialectFactoryImpl implements DialectFactory {
   private ClassLoaderService classLoaderService;
   private DialectResolver dialectResolver;

   @InjectService
   public void setClassLoaderService(ClassLoaderService classLoaderService) {
      this.classLoaderService = classLoaderService;
   }

   @InjectService
   public void setDialectResolver(DialectResolver dialectResolver) {
      this.dialectResolver = dialectResolver;
   }

   public Dialect buildDialect(Map configValues, Connection connection) throws HibernateException {
      String dialectName = (String)configValues.get("hibernate.dialect");
      return dialectName != null ? this.constructDialect(dialectName) : this.determineDialect(connection);
   }

   private Dialect constructDialect(String dialectName) {
      try {
         return (Dialect)this.classLoaderService.classForName(dialectName).newInstance();
      } catch (ClassLoadingException e) {
         throw new HibernateException("Dialect class not found: " + dialectName, e);
      } catch (HibernateException e) {
         throw e;
      } catch (Exception e) {
         throw new HibernateException("Could not instantiate dialect class", e);
      }
   }

   private Dialect determineDialect(Connection connection) {
      if (connection == null) {
         throw new HibernateException("Connection cannot be null when 'hibernate.dialect' not set");
      } else {
         try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            Dialect dialect = this.dialectResolver.resolveDialect(databaseMetaData);
            if (dialect == null) {
               throw new HibernateException("Unable to determine Dialect to use [name=" + databaseMetaData.getDatabaseProductName() + ", majorVersion=" + databaseMetaData.getDatabaseMajorVersion() + "]; user must register resolver or explicitly set 'hibernate.dialect'");
            } else {
               return dialect;
            }
         } catch (SQLException sqlException) {
            throw new HibernateException("Unable to access java.sql.DatabaseMetaData to determine appropriate Dialect to use", sqlException);
         }
      }
   }
}
