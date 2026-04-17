package org.hibernate.exception.spi;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.dialect.Dialect;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public class SQLExceptionConverterFactory {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SQLExceptionConverterFactory.class.getName());

   private SQLExceptionConverterFactory() {
   }

   public static SQLExceptionConverter buildSQLExceptionConverter(Dialect dialect, Properties properties) throws HibernateException {
      SQLExceptionConverter converter = null;
      String converterClassName = (String)properties.get("hibernate.jdbc.sql_exception_converter");
      if (StringHelper.isNotEmpty(converterClassName)) {
         converter = constructConverter(converterClassName, dialect.getViolatedConstraintNameExtracter());
      }

      if (converter == null) {
         LOG.trace("Using dialect defined converter");
         converter = dialect.buildSQLExceptionConverter();
      }

      if (converter instanceof Configurable) {
         try {
            ((Configurable)converter).configure(properties);
         } catch (HibernateException e) {
            LOG.unableToConfigureSqlExceptionConverter(e);
            throw e;
         }
      }

      return converter;
   }

   public static SQLExceptionConverter buildMinimalSQLExceptionConverter() {
      return new SQLExceptionConverter() {
         public JDBCException convert(SQLException sqlException, String message, String sql) {
            return new GenericJDBCException(message, sqlException, sql);
         }
      };
   }

   private static SQLExceptionConverter constructConverter(String converterClassName, ViolatedConstraintNameExtracter violatedConstraintNameExtracter) {
      try {
         LOG.tracev("Attempting to construct instance of specified SQLExceptionConverter [{0}]", converterClassName);
         Class converterClass = ReflectHelper.classForName(converterClassName);
         Constructor[] ctors = converterClass.getDeclaredConstructors();

         for(int i = 0; i < ctors.length; ++i) {
            if (ctors[i].getParameterTypes() != null && ctors[i].getParameterTypes().length == 1 && ViolatedConstraintNameExtracter.class.isAssignableFrom(ctors[i].getParameterTypes()[0])) {
               try {
                  return (SQLExceptionConverter)ctors[i].newInstance(violatedConstraintNameExtracter);
               } catch (Throwable var6) {
               }
            }
         }

         return (SQLExceptionConverter)converterClass.newInstance();
      } catch (Throwable t) {
         LOG.unableToConstructSqlExceptionConverter(t);
         return null;
      }
   }
}
