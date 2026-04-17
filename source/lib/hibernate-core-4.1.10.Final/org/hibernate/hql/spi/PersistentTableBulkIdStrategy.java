package org.hibernate.hql.spi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.cfg.Mappings;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.internal.AbstractSessionImpl;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.SelectValues;
import org.hibernate.type.UUIDCharType;
import org.jboss.logging.Logger;

public class PersistentTableBulkIdStrategy implements MultiTableBulkIdStrategy {
   private static final CoreMessageLogger log = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, PersistentTableBulkIdStrategy.class.getName());
   public static final String SHORT_NAME = "persistent";
   public static final String CLEAN_UP_ID_TABLES = "hibernate.hql.bulk_id_strategy.persistent.clean_up";
   public static final String SCHEMA = "hibernate.hql.bulk_id_strategy.persistent.schema";
   public static final String CATALOG = "hibernate.hql.bulk_id_strategy.persistent.catalog";
   private String catalog;
   private String schema;
   private boolean cleanUpTables;
   private List tableCleanUpDdl;

   public void prepare(JdbcServices jdbcServices, JdbcConnectionAccess connectionAccess, Mappings mappings, Mapping mapping, Map settings) {
      this.catalog = ConfigurationHelper.getString("hibernate.hql.bulk_id_strategy.persistent.catalog", settings, ConfigurationHelper.getString("hibernate.default_catalog", settings));
      this.schema = ConfigurationHelper.getString("hibernate.hql.bulk_id_strategy.persistent.schema", settings, ConfigurationHelper.getString("hibernate.default_schema", settings));
      this.cleanUpTables = ConfigurationHelper.getBoolean("hibernate.hql.bulk_id_strategy.persistent.clean_up", settings, false);
      Iterator<PersistentClass> entityMappings = mappings.iterateClasses();
      List<Table> idTableDefinitions = new ArrayList();

      while(entityMappings.hasNext()) {
         PersistentClass entityMapping = (PersistentClass)entityMappings.next();
         Table idTableDefinition = this.generateIdTableDefinition(entityMapping);
         idTableDefinitions.add(idTableDefinition);
      }

      this.exportTableDefinitions(idTableDefinitions, jdbcServices, connectionAccess, mappings, mapping);
   }

   protected Table generateIdTableDefinition(PersistentClass entityMapping) {
      Table idTable = new Table(entityMapping.getTemporaryIdTableName());
      if (this.catalog != null) {
         idTable.setCatalog(this.catalog);
      }

      if (this.schema != null) {
         idTable.setSchema(this.schema);
      }

      Iterator itr = entityMapping.getTable().getPrimaryKey().getColumnIterator();

      while(itr.hasNext()) {
         Column column = (Column)itr.next();
         idTable.addColumn(column.clone());
      }

      Column sessionIdColumn = new Column("hib_sess_id");
      sessionIdColumn.setSqlType("CHAR(36)");
      sessionIdColumn.setComment("Used to hold the Hibernate Session identifier");
      idTable.addColumn(sessionIdColumn);
      idTable.setComment("Used to hold id values for the " + entityMapping.getEntityName() + " class");
      return idTable;
   }

   protected void exportTableDefinitions(List idTableDefinitions, JdbcServices jdbcServices, JdbcConnectionAccess connectionAccess, Mappings mappings, Mapping mapping) {
      try {
         Connection connection;
         try {
            connection = connectionAccess.obtainConnection();
         } catch (UnsupportedOperationException var23) {
            log.debug("Unable to obtain JDBC connection; assuming ID tables already exist or wont be needed");
            return;
         }

         try {
            Statement statement = connection.createStatement();

            for(Table idTableDefinition : idTableDefinitions) {
               if (this.cleanUpTables) {
                  if (this.tableCleanUpDdl == null) {
                     this.tableCleanUpDdl = new ArrayList();
                  }

                  this.tableCleanUpDdl.add(idTableDefinition.sqlDropString(jdbcServices.getDialect(), (String)null, (String)null));
               }

               try {
                  String sql = idTableDefinition.sqlCreateString(jdbcServices.getDialect(), mapping, (String)null, (String)null);
                  jdbcServices.getSqlStatementLogger().logStatement(sql);
                  statement.execute(sql);
               } catch (SQLException e) {
                  log.debugf("Error attempting to export id-table [%s] : %s", idTableDefinition.getName(), e.getMessage());
               }
            }

            statement.close();
         } catch (SQLException e) {
            log.error("Unable to use JDBC Connection to create Statement", e);
         } finally {
            try {
               connectionAccess.releaseConnection(connection);
            } catch (SQLException var21) {
            }

         }
      } catch (SQLException e) {
         log.error("Unable obtain JDBC Connection", e);
      }

   }

   public void release(JdbcServices jdbcServices, JdbcConnectionAccess connectionAccess) {
      if (this.cleanUpTables && this.tableCleanUpDdl != null) {
         try {
            Connection connection = connectionAccess.obtainConnection();

            try {
               Statement statement = connection.createStatement();

               for(String cleanupDdl : this.tableCleanUpDdl) {
                  try {
                     jdbcServices.getSqlStatementLogger().logStatement(cleanupDdl);
                     statement.execute(cleanupDdl);
                  } catch (SQLException e) {
                     log.debugf("Error attempting to cleanup id-table : [%s]", e.getMessage());
                  }
               }

               statement.close();
            } catch (SQLException e) {
               log.error("Unable to use JDBC Connection to create Statement", e);
            } finally {
               try {
                  connectionAccess.releaseConnection(connection);
               } catch (SQLException var17) {
               }

            }
         } catch (SQLException e) {
            log.error("Unable obtain JDBC Connection", e);
         }

      }
   }

   public MultiTableBulkIdStrategy.UpdateHandler buildUpdateHandler(SessionFactoryImplementor factory, HqlSqlWalker walker) {
      return new TableBasedUpdateHandlerImpl(factory, walker, this.catalog, this.schema) {
         protected void addAnyExtraIdSelectValues(SelectValues selectClause) {
            selectClause.addParameter(1, 36);
         }

         protected String generateIdSubselect(Queryable persister) {
            return super.generateIdSubselect(persister) + " where hib_sess_id=?";
         }

         protected int handlePrependedParametersOnIdSelection(PreparedStatement ps, SessionImplementor session, int pos) throws SQLException {
            PersistentTableBulkIdStrategy.this.bindSessionIdentifier(ps, session, pos);
            return 1;
         }

         protected void handleAddedParametersOnUpdate(PreparedStatement ps, SessionImplementor session, int position) throws SQLException {
            PersistentTableBulkIdStrategy.this.bindSessionIdentifier(ps, session, position);
         }

         protected void releaseFromUse(Queryable persister, SessionImplementor session) {
            PersistentTableBulkIdStrategy.this.cleanUpRows(this.determineIdTableName(persister), session);
         }
      };
   }

   private void bindSessionIdentifier(PreparedStatement ps, SessionImplementor session, int position) throws SQLException {
      if (!AbstractSessionImpl.class.isInstance(session)) {
         throw new HibernateException("Only available on SessionImpl instances");
      } else {
         UUIDCharType.INSTANCE.set(ps, ((AbstractSessionImpl)session).getSessionIdentifier(), position, session);
      }
   }

   private void cleanUpRows(String tableName, SessionImplementor session) {
      String sql = "delete from " + tableName + " where hib_sess_id=?";

      try {
         PreparedStatement ps = null;

         try {
            ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, false);
            this.bindSessionIdentifier(ps, session, 1);
            ps.executeUpdate();
         } finally {
            if (ps != null) {
               try {
                  ps.close();
               } catch (Throwable var12) {
               }
            }

         }

      } catch (SQLException e) {
         throw this.convert(session.getFactory(), e, "Unable to clean up id table [" + tableName + "]", sql);
      }
   }

   protected JDBCException convert(SessionFactoryImplementor factory, SQLException e, String message, String sql) {
      throw factory.getSQLExceptionHelper().convert(e, message, sql);
   }

   public MultiTableBulkIdStrategy.DeleteHandler buildDeleteHandler(SessionFactoryImplementor factory, HqlSqlWalker walker) {
      return new TableBasedDeleteHandlerImpl(factory, walker, this.catalog, this.schema) {
         protected void addAnyExtraIdSelectValues(SelectValues selectClause) {
            selectClause.addParameter(1, 36);
         }

         protected String generateIdSubselect(Queryable persister) {
            return super.generateIdSubselect(persister) + " where hib_sess_id=?";
         }

         protected int handlePrependedParametersOnIdSelection(PreparedStatement ps, SessionImplementor session, int pos) throws SQLException {
            PersistentTableBulkIdStrategy.this.bindSessionIdentifier(ps, session, pos);
            return 1;
         }

         protected void handleAddedParametersOnDelete(PreparedStatement ps, SessionImplementor session) throws SQLException {
            PersistentTableBulkIdStrategy.this.bindSessionIdentifier(ps, session, 1);
         }

         protected void releaseFromUse(Queryable persister, SessionImplementor session) {
            PersistentTableBulkIdStrategy.this.cleanUpRows(this.determineIdTableName(persister), session);
         }
      };
   }
}
