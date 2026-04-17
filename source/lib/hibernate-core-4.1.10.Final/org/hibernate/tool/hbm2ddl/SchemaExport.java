package org.hibernate.tool.hbm2ddl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.hibernate.HibernateException;
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
import org.hibernate.internal.util.ConfigHelper;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.config.spi.ConfigurationService;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.jboss.logging.Logger;

public class SchemaExport {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SchemaExport.class.getName());
   private static final String DEFAULT_IMPORT_FILE = "/import.sql";
   private final ConnectionHelper connectionHelper;
   private final SqlStatementLogger sqlStatementLogger;
   private final SqlExceptionHelper sqlExceptionHelper;
   private final String[] dropSQL;
   private final String[] createSQL;
   private final String importFiles;
   private final List exceptions;
   private Formatter formatter;
   private ImportSqlCommandExtractor importSqlCommandExtractor;
   private String outputFile;
   private String delimiter;
   private boolean haltOnError;

   public SchemaExport(ServiceRegistry serviceRegistry, Configuration configuration) {
      this.exceptions = new ArrayList();
      this.importSqlCommandExtractor = ImportSqlCommandExtractorInitiator.DEFAULT_EXTRACTOR;
      this.outputFile = null;
      this.haltOnError = false;
      this.connectionHelper = new SuppliedConnectionProviderConnectionHelper((ConnectionProvider)serviceRegistry.getService(ConnectionProvider.class));
      this.sqlStatementLogger = ((JdbcServices)serviceRegistry.getService(JdbcServices.class)).getSqlStatementLogger();
      this.formatter = (this.sqlStatementLogger.isFormat() ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
      this.sqlExceptionHelper = ((JdbcServices)serviceRegistry.getService(JdbcServices.class)).getSqlExceptionHelper();
      this.importFiles = ConfigurationHelper.getString("hibernate.hbm2ddl.import_files", configuration.getProperties(), "/import.sql");
      Dialect dialect = ((JdbcServices)serviceRegistry.getService(JdbcServices.class)).getDialect();
      this.dropSQL = configuration.generateDropSchemaScript(dialect);
      this.createSQL = configuration.generateSchemaCreationScript(dialect);
   }

   public SchemaExport(MetadataImplementor metadata) {
      this.exceptions = new ArrayList();
      this.importSqlCommandExtractor = ImportSqlCommandExtractorInitiator.DEFAULT_EXTRACTOR;
      this.outputFile = null;
      this.haltOnError = false;
      ServiceRegistry serviceRegistry = metadata.getServiceRegistry();
      this.connectionHelper = new SuppliedConnectionProviderConnectionHelper((ConnectionProvider)serviceRegistry.getService(ConnectionProvider.class));
      JdbcServices jdbcServices = (JdbcServices)serviceRegistry.getService(JdbcServices.class);
      this.sqlStatementLogger = jdbcServices.getSqlStatementLogger();
      this.formatter = (this.sqlStatementLogger.isFormat() ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
      this.sqlExceptionHelper = jdbcServices.getSqlExceptionHelper();
      this.importFiles = ConfigurationHelper.getString("hibernate.hbm2ddl.import_files", ((ConfigurationService)serviceRegistry.getService(ConfigurationService.class)).getSettings(), "/import.sql");
      Dialect dialect = jdbcServices.getDialect();
      this.dropSQL = metadata.getDatabase().generateDropSchemaScript(dialect);
      this.createSQL = metadata.getDatabase().generateSchemaCreationScript(dialect);
   }

   public SchemaExport(Configuration configuration) {
      this(configuration, configuration.getProperties());
   }

   /** @deprecated */
   @Deprecated
   public SchemaExport(Configuration configuration, Properties properties) throws HibernateException {
      this.exceptions = new ArrayList();
      this.importSqlCommandExtractor = ImportSqlCommandExtractorInitiator.DEFAULT_EXTRACTOR;
      this.outputFile = null;
      this.haltOnError = false;
      Dialect dialect = Dialect.getDialect(properties);
      Properties props = new Properties();
      props.putAll(dialect.getDefaultProperties());
      props.putAll(properties);
      this.connectionHelper = new ManagedProviderConnectionHelper(props);
      this.sqlStatementLogger = new SqlStatementLogger(false, true);
      this.formatter = FormatStyle.DDL.getFormatter();
      this.sqlExceptionHelper = new SqlExceptionHelper();
      this.importFiles = ConfigurationHelper.getString("hibernate.hbm2ddl.import_files", properties, "/import.sql");
      this.dropSQL = configuration.generateDropSchemaScript(dialect);
      this.createSQL = configuration.generateSchemaCreationScript(dialect);
   }

   public SchemaExport(Configuration configuration, Connection connection) throws HibernateException {
      this.exceptions = new ArrayList();
      this.importSqlCommandExtractor = ImportSqlCommandExtractorInitiator.DEFAULT_EXTRACTOR;
      this.outputFile = null;
      this.haltOnError = false;
      this.connectionHelper = new SuppliedConnectionHelper(connection);
      this.sqlStatementLogger = new SqlStatementLogger(false, true);
      this.formatter = FormatStyle.DDL.getFormatter();
      this.sqlExceptionHelper = new SqlExceptionHelper();
      this.importFiles = ConfigurationHelper.getString("hibernate.hbm2ddl.import_files", configuration.getProperties(), "/import.sql");
      Dialect dialect = Dialect.getDialect(configuration.getProperties());
      this.dropSQL = configuration.generateDropSchemaScript(dialect);
      this.createSQL = configuration.generateSchemaCreationScript(dialect);
   }

   public SchemaExport(ConnectionHelper connectionHelper, String[] dropSql, String[] createSql) {
      this.exceptions = new ArrayList();
      this.importSqlCommandExtractor = ImportSqlCommandExtractorInitiator.DEFAULT_EXTRACTOR;
      this.outputFile = null;
      this.haltOnError = false;
      this.connectionHelper = connectionHelper;
      this.dropSQL = dropSql;
      this.createSQL = createSql;
      this.importFiles = "";
      this.sqlStatementLogger = new SqlStatementLogger(false, true);
      this.sqlExceptionHelper = new SqlExceptionHelper();
      this.formatter = FormatStyle.DDL.getFormatter();
   }

   public SchemaExport setOutputFile(String filename) {
      this.outputFile = filename;
      return this;
   }

   public SchemaExport setDelimiter(String delimiter) {
      this.delimiter = delimiter;
      return this;
   }

   public SchemaExport setFormat(boolean format) {
      this.formatter = (format ? FormatStyle.DDL : FormatStyle.NONE).getFormatter();
      return this;
   }

   public SchemaExport setImportSqlCommandExtractor(ImportSqlCommandExtractor importSqlCommandExtractor) {
      this.importSqlCommandExtractor = importSqlCommandExtractor;
      return this;
   }

   public SchemaExport setHaltOnError(boolean haltOnError) {
      this.haltOnError = haltOnError;
      return this;
   }

   public void create(boolean script, boolean export) {
      this.create(Target.interpret(script, export));
   }

   public void create(Target output) {
      this.execute(output, SchemaExport.Type.BOTH);
   }

   public void drop(boolean script, boolean export) {
      this.drop(Target.interpret(script, export));
   }

   public void drop(Target output) {
      this.execute(output, SchemaExport.Type.DROP);
   }

   public void execute(boolean script, boolean export, boolean justDrop, boolean justCreate) {
      this.execute(Target.interpret(script, export), this.interpretType(justDrop, justCreate));
   }

   private Type interpretType(boolean justDrop, boolean justCreate) {
      if (justDrop) {
         return SchemaExport.Type.DROP;
      } else {
         return justCreate ? SchemaExport.Type.CREATE : SchemaExport.Type.BOTH;
      }
   }

   public void execute(Target output, Type type) {
      if ((this.outputFile != null || output != Target.NONE) && type != SchemaExport.Type.NONE) {
         this.exceptions.clear();
         LOG.runningHbm2ddlSchemaExport();
         List<NamedReader> importFileReaders = new ArrayList();

         for(String currentFile : this.importFiles.split(",")) {
            try {
               String resourceName = currentFile.trim();
               InputStream stream = ConfigHelper.getResourceAsStream(resourceName);
               importFileReaders.add(new NamedReader(resourceName, stream));
            } catch (HibernateException var25) {
               LOG.debugf("Import file not found: %s", currentFile);
            }
         }

         List<Exporter> exporters = new ArrayList();

         try {
            if (output.doScript()) {
               exporters.add(new ScriptExporter());
            }

            if (this.outputFile != null) {
               exporters.add(new FileExporter(this.outputFile));
            }

            if (output.doExport()) {
               exporters.add(new DatabaseExporter(this.connectionHelper, this.sqlExceptionHelper));
            }

            if (type.doDrop()) {
               this.perform(this.dropSQL, exporters);
            }

            if (type.doCreate()) {
               this.perform(this.createSQL, exporters);
               if (!importFileReaders.isEmpty()) {
                  for(NamedReader namedReader : importFileReaders) {
                     this.importScript(namedReader, exporters);
                  }
               }
            }
         } catch (Exception e) {
            this.exceptions.add(e);
            LOG.schemaExportUnsuccessful(e);
         } finally {
            for(Exporter exporter : exporters) {
               try {
                  exporter.release();
               } catch (Exception var24) {
               }
            }

            for(NamedReader namedReader : importFileReaders) {
               try {
                  namedReader.getReader().close();
               } catch (Exception var23) {
               }
            }

            LOG.schemaExportComplete();
         }

      }
   }

   private void perform(String[] sqlCommands, List exporters) {
      for(String sqlCommand : sqlCommands) {
         String formatted = this.formatter.format(sqlCommand);
         if (this.delimiter != null) {
            formatted = formatted + this.delimiter;
         }

         this.sqlStatementLogger.logStatement(sqlCommand, this.formatter);

         for(Exporter exporter : exporters) {
            try {
               exporter.export(formatted);
            } catch (Exception e) {
               if (this.haltOnError) {
                  throw new HibernateException("Error during DDL export", e);
               }

               this.exceptions.add(e);
               LOG.unsuccessfulCreate(sqlCommand);
               LOG.error(e.getMessage());
            }
         }
      }

   }

   private void importScript(NamedReader namedReader, List exporters) throws Exception {
      BufferedReader reader = new BufferedReader(namedReader.getReader());
      String[] statements = this.importSqlCommandExtractor.extractCommands(reader);
      if (statements != null) {
         for(String statement : statements) {
            if (statement != null) {
               String trimmedSql = statement.trim();
               if (trimmedSql.endsWith(";")) {
                  trimmedSql = trimmedSql.substring(0, statement.length() - 1);
               }

               if (!StringHelper.isEmpty(trimmedSql)) {
                  try {
                     for(Exporter exporter : exporters) {
                        if (exporter.acceptsImportScripts()) {
                           exporter.export(trimmedSql);
                        }
                     }
                  } catch (Exception e) {
                     throw new ImportScriptException("Error during statement execution (file: '" + namedReader.getName() + "'): " + trimmedSql, e);
                  }
               }
            }
         }
      }

   }

   private void execute(boolean script, boolean export, Writer fileOutput, Statement statement, String sql) throws IOException, SQLException {
      SqlExceptionHelper sqlExceptionHelper = new SqlExceptionHelper();
      String formatted = this.formatter.format(sql);
      if (this.delimiter != null) {
         formatted = formatted + this.delimiter;
      }

      if (script) {
         System.out.println(formatted);
      }

      LOG.debug(formatted);
      if (this.outputFile != null) {
         fileOutput.write(formatted + "\n");
      }

      if (export) {
         statement.executeUpdate(sql);

         try {
            SQLWarning warnings = statement.getWarnings();
            if (warnings != null) {
               sqlExceptionHelper.logAndClearWarnings(this.connectionHelper.getConnection());
            }
         } catch (SQLException sqle) {
            LOG.unableToLogSqlWarnings(sqle);
         }
      }

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
         boolean drop = false;
         boolean create = false;
         boolean halt = false;
         boolean export = true;
         String outFile = null;
         String importFile = "/import.sql";
         String propFile = null;
         boolean format = false;
         String delim = null;

         for(int i = 0; i < args.length; ++i) {
            if (args[i].startsWith("--")) {
               if (args[i].equals("--quiet")) {
                  script = false;
               } else if (args[i].equals("--drop")) {
                  drop = true;
               } else if (args[i].equals("--create")) {
                  create = true;
               } else if (args[i].equals("--haltonerror")) {
                  halt = true;
               } else if (args[i].equals("--text")) {
                  export = false;
               } else if (args[i].startsWith("--output=")) {
                  outFile = args[i].substring(9);
               } else if (args[i].startsWith("--import=")) {
                  importFile = args[i].substring(9);
               } else if (args[i].startsWith("--properties=")) {
                  propFile = args[i].substring(13);
               } else if (args[i].equals("--format")) {
                  format = true;
               } else if (args[i].startsWith("--delimiter=")) {
                  delim = args[i].substring(12);
               } else if (args[i].startsWith("--config=")) {
                  cfg.configure(args[i].substring(9));
               } else if (args[i].startsWith("--naming=")) {
                  cfg.setNamingStrategy((NamingStrategy)ReflectHelper.classForName(args[i].substring(9)).newInstance());
               }
            } else {
               String filename = args[i];
               if (filename.endsWith(".jar")) {
                  cfg.addJar(new File(filename));
               } else {
                  cfg.addFile(filename);
               }
            }
         }

         if (propFile != null) {
            Properties props = new Properties();
            props.putAll(cfg.getProperties());
            props.load(new FileInputStream(propFile));
            cfg.setProperties(props);
         }

         if (importFile != null) {
            cfg.setProperty("hibernate.hbm2ddl.import_files", importFile);
         }

         StandardServiceRegistryImpl serviceRegistry = createServiceRegistry(cfg.getProperties());

         try {
            SchemaExport se = (new SchemaExport(serviceRegistry, cfg)).setHaltOnError(halt).setOutputFile(outFile).setDelimiter(delim).setImportSqlCommandExtractor((ImportSqlCommandExtractor)serviceRegistry.getService(ImportSqlCommandExtractor.class));
            if (format) {
               se.setFormat(true);
            }

            se.execute(script, export, drop, create);
         } finally {
            serviceRegistry.destroy();
         }
      } catch (Exception e) {
         LOG.unableToCreateSchema(e);
         e.printStackTrace();
      }

   }

   public List getExceptions() {
      return this.exceptions;
   }

   public static enum Type {
      CREATE,
      DROP,
      NONE,
      BOTH;

      public boolean doCreate() {
         return this == BOTH || this == CREATE;
      }

      public boolean doDrop() {
         return this == BOTH || this == DROP;
      }
   }

   private static class NamedReader {
      private final Reader reader;
      private final String name;

      public NamedReader(String name, InputStream stream) {
         this.name = name;
         this.reader = new InputStreamReader(stream);
      }

      public Reader getReader() {
         return this.reader;
      }

      public String getName() {
         return this.name;
      }
   }
}
