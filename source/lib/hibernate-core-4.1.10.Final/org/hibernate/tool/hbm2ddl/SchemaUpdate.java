package org.hibernate.tool.hbm2ddl;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.jboss.logging.Logger;

public class SchemaUpdate {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SchemaUpdate.class.getName());
   private final Configuration configuration;
   private final ConnectionHelper connectionHelper;
   private final SqlStatementLogger sqlStatementLogger;
   private final SqlExceptionHelper sqlExceptionHelper;
   private final Dialect dialect;
   private final List exceptions;
   private Formatter formatter;
   private boolean haltOnError;
   private boolean format;
   private String outputFile;
   private String delimiter;

   public SchemaUpdate(Configuration cfg) throws HibernateException {
      this(cfg, cfg.getProperties());
   }

   public SchemaUpdate(Configuration configuration, Properties properties) throws HibernateException {
      this.exceptions = new ArrayList();
      this.haltOnError = false;
      this.format = true;
      this.outputFile = null;
      this.configuration = configuration;
      this.dialect = Dialect.getDialect(properties);
      Properties props = new Properties();
      props.putAll(this.dialect.getDefaultProperties());
      props.putAll(properties);
      this.connectionHelper = new ManagedProviderConnectionHelper(props);
      this.sqlExceptionHelper = new SqlExceptionHelper();
      this.sqlStatementLogger = new SqlStatementLogger(false, true);
      this.formatter = FormatStyle.DDL.getFormatter();
   }

   public SchemaUpdate(ServiceRegistry serviceRegistry, Configuration cfg) throws HibernateException {
      this.exceptions = new ArrayList();
      this.haltOnError = false;
      this.format = true;
      this.outputFile = null;
      this.configuration = cfg;
      JdbcServices jdbcServices = (JdbcServices)serviceRegistry.getService(JdbcServices.class);
      this.dialect = jdbcServices.getDialect();
      this.connectionHelper = new SuppliedConnectionProviderConnectionHelper(jdbcServices.getConnectionProvider());
      this.sqlExceptionHelper = new SqlExceptionHelper();
      this.sqlStatementLogger = jdbcServices.getSqlStatementLogger();
      this.formatter = (this.sqlStatementLogger.isFormat() ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
   }

   private static StandardServiceRegistryImpl createServiceRegistry(Properties properties) {
      Environment.verifyProperties(properties);
      ConfigurationHelper.resolvePlaceHolders(properties);
      return (StandardServiceRegistryImpl)(new ServiceRegistryBuilder()).applySettings(properties).buildServiceRegistry();
   }

   public static void main(String[] args) {
      try {
         Configuration cfg = new Configuration();
         boolean script = true;
         boolean doUpdate = true;
         String propFile = null;

         for(int i = 0; i < args.length; ++i) {
            if (args[i].startsWith("--")) {
               if (args[i].equals("--quiet")) {
                  script = false;
               } else if (args[i].startsWith("--properties=")) {
                  propFile = args[i].substring(13);
               } else if (args[i].startsWith("--config=")) {
                  cfg.configure(args[i].substring(9));
               } else if (args[i].startsWith("--text")) {
                  doUpdate = false;
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
            (new SchemaUpdate(serviceRegistry, cfg)).execute(script, doUpdate);
         } finally {
            serviceRegistry.destroy();
         }
      } catch (Exception e) {
         LOG.unableToRunSchemaUpdate(e);
         e.printStackTrace();
      }

   }

   public void execute(boolean script, boolean doUpdate) {
      this.execute(Target.interpret(script, doUpdate));
   }

   public void execute(Target target) {
      LOG.runningHbm2ddlSchemaUpdate();
      Connection connection = null;
      Statement stmt = null;
      Writer outputFileWriter = null;
      this.exceptions.clear();

      try {
         DatabaseMetadata meta;
         try {
            LOG.fetchingDatabaseMetadata();
            this.connectionHelper.prepare(true);
            connection = this.connectionHelper.getConnection();
            meta = new DatabaseMetadata(connection, this.dialect);
            stmt = connection.createStatement();
         } catch (SQLException sqle) {
            this.exceptions.add(sqle);
            LOG.unableToGetDatabaseMetadata(sqle);
            throw sqle;
         }

         LOG.updatingSchema();
         if (this.outputFile != null) {
            LOG.writingGeneratedSchemaToFile(this.outputFile);
            outputFileWriter = new FileWriter(this.outputFile);
         }

         String[] sqlStrings = this.configuration.generateSchemaUpdateScript(this.dialect, meta);

         for(String sql : sqlStrings) {
            String formatted = this.formatter.format(sql);

            try {
               if (this.delimiter != null) {
                  formatted = formatted + this.delimiter;
               }

               if (target.doScript()) {
                  System.out.println(formatted);
               }

               if (this.outputFile != null) {
                  outputFileWriter.write(formatted + "\n");
               }

               if (target.doExport()) {
                  LOG.debug(sql);
                  stmt.executeUpdate(formatted);
               }
            } catch (SQLException e) {
               if (this.haltOnError) {
                  throw new JDBCException("Error during DDL export", e);
               }

               this.exceptions.add(e);
               LOG.unsuccessful(sql);
               LOG.error(e.getMessage());
            }
         }

         LOG.schemaUpdateComplete();
      } catch (Exception e) {
         this.exceptions.add(e);
         LOG.unableToCompleteSchemaUpdate(e);
      } finally {
         try {
            if (stmt != null) {
               stmt.close();
            }

            this.connectionHelper.release();
         } catch (Exception e) {
            this.exceptions.add(e);
            LOG.unableToCloseConnection(e);
         }

         try {
            if (outputFileWriter != null) {
               outputFileWriter.close();
            }
         } catch (Exception e) {
            this.exceptions.add(e);
            LOG.unableToCloseConnection(e);
         }

      }

   }

   public List getExceptions() {
      return this.exceptions;
   }

   public void setHaltOnError(boolean haltOnError) {
      this.haltOnError = haltOnError;
   }

   public void setFormat(boolean format) {
      this.formatter = (format ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
   }

   public void setOutputFile(String outputFile) {
      this.outputFile = outputFile;
   }

   public void setDelimiter(String delimiter) {
      this.delimiter = delimiter;
   }
}
