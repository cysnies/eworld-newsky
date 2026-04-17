package org.hibernate.tool.hbm2ddl;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.jboss.logging.Logger;

public class SchemaValidator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SchemaValidator.class.getName());
   private ConnectionHelper connectionHelper;
   private Configuration configuration;
   private Dialect dialect;

   public SchemaValidator(Configuration cfg) throws HibernateException {
      this(cfg, cfg.getProperties());
   }

   public SchemaValidator(Configuration cfg, Properties connectionProperties) throws HibernateException {
      this.configuration = cfg;
      this.dialect = Dialect.getDialect(connectionProperties);
      Properties props = new Properties();
      props.putAll(this.dialect.getDefaultProperties());
      props.putAll(connectionProperties);
      this.connectionHelper = new ManagedProviderConnectionHelper(props);
   }

   public SchemaValidator(ServiceRegistry serviceRegistry, Configuration cfg) throws HibernateException {
      this.configuration = cfg;
      JdbcServices jdbcServices = (JdbcServices)serviceRegistry.getService(JdbcServices.class);
      this.dialect = jdbcServices.getDialect();
      this.connectionHelper = new SuppliedConnectionProviderConnectionHelper(jdbcServices.getConnectionProvider());
   }

   private static StandardServiceRegistryImpl createServiceRegistry(Properties properties) {
      Environment.verifyProperties(properties);
      ConfigurationHelper.resolvePlaceHolders(properties);
      return (StandardServiceRegistryImpl)(new ServiceRegistryBuilder()).applySettings(properties).buildServiceRegistry();
   }

   public static void main(String[] args) {
      try {
         Configuration cfg = new Configuration();
         String propFile = null;

         for(int i = 0; i < args.length; ++i) {
            if (args[i].startsWith("--")) {
               if (args[i].startsWith("--properties=")) {
                  propFile = args[i].substring(13);
               } else if (args[i].startsWith("--config=")) {
                  cfg.configure(args[i].substring(9));
               } else if (args[i].startsWith("--naming=")) {
                  cfg.setNamingStrategy((NamingStrategy)ReflectHelper.classForName(args[i].substring(9)).newInstance());
               }
            } else {
               cfg.addFile(args[i]);
            }
         }

         if (propFile != null) {
            Properties props = new Properties();
            props.putAll(cfg.getProperties());
            props.load(new FileInputStream(propFile));
            cfg.setProperties(props);
         }

         StandardServiceRegistryImpl serviceRegistry = createServiceRegistry(cfg.getProperties());

         try {
            (new SchemaValidator(serviceRegistry, cfg)).validate();
         } finally {
            serviceRegistry.destroy();
         }
      } catch (Exception e) {
         LOG.unableToRunSchemaUpdate(e);
         e.printStackTrace();
      }

   }

   public void validate() {
      LOG.runningSchemaValidator();
      Connection connection = null;

      try {
         DatabaseMetadata meta;
         try {
            LOG.fetchingDatabaseMetadata();
            this.connectionHelper.prepare(false);
            connection = this.connectionHelper.getConnection();
            meta = new DatabaseMetadata(connection, this.dialect, false);
         } catch (SQLException sqle) {
            LOG.unableToGetDatabaseMetadata(sqle);
            throw sqle;
         }

         this.configuration.validateSchema(this.dialect, meta);
      } catch (SQLException e) {
         LOG.unableToCompleteSchemaValidation(e);
      } finally {
         try {
            this.connectionHelper.release();
         } catch (Exception e) {
            LOG.unableToCloseConnection(e);
         }

      }

   }
}
