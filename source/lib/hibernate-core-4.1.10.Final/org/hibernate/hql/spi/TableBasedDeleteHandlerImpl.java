package org.hibernate.hql.spi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.tree.DeleteStatement;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.Delete;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class TableBasedDeleteHandlerImpl extends AbstractTableBasedBulkIdHandler implements MultiTableBulkIdStrategy.DeleteHandler {
   private static final Logger log = Logger.getLogger(TableBasedDeleteHandlerImpl.class);
   private final Queryable targetedPersister;
   private final String idInsertSelect;
   private final List idSelectParameterSpecifications;
   private final List deletes;

   public TableBasedDeleteHandlerImpl(SessionFactoryImplementor factory, HqlSqlWalker walker) {
      this(factory, walker, (String)null, (String)null);
   }

   public TableBasedDeleteHandlerImpl(SessionFactoryImplementor factory, HqlSqlWalker walker, String catalog, String schema) {
      super(factory, walker, catalog, schema);
      DeleteStatement deleteStatement = (DeleteStatement)walker.getAST();
      FromElement fromElement = deleteStatement.getFromClause().getFromElement();
      this.targetedPersister = fromElement.getQueryable();
      String bulkTargetAlias = fromElement.getTableAlias();
      AbstractTableBasedBulkIdHandler.ProcessedWhereClause processedWhereClause = this.processWhereClause(deleteStatement.getWhereClause());
      this.idSelectParameterSpecifications = processedWhereClause.getIdSelectParameterSpecifications();
      this.idInsertSelect = this.generateIdInsertSelect(this.targetedPersister, bulkTargetAlias, processedWhereClause);
      log.tracev("Generated ID-INSERT-SELECT SQL (multi-table delete) : {0}", this.idInsertSelect);
      String idSubselect = this.generateIdSubselect(this.targetedPersister);
      this.deletes = new ArrayList();

      for(Type type : this.targetedPersister.getPropertyTypes()) {
         if (type.isCollectionType()) {
            CollectionType cType = (CollectionType)type;
            AbstractCollectionPersister cPersister = (AbstractCollectionPersister)factory.getCollectionPersister(cType.getRole());
            if (cPersister.isManyToMany()) {
               this.deletes.add(this.generateDelete(cPersister.getTableName(), cPersister.getKeyColumnNames(), idSubselect, "bulk delete - m2m join table cleanup"));
            }
         }
      }

      String[] tableNames = this.targetedPersister.getConstraintOrderedTableNameClosure();
      String[][] columnNames = this.targetedPersister.getContraintOrderedTableKeyColumnClosure();

      for(int i = 0; i < tableNames.length; ++i) {
         this.deletes.add(this.generateDelete(tableNames[i], columnNames[i], idSubselect, "bulk delete"));
      }

   }

   private String generateDelete(String tableName, String[] columnNames, String idSubselect, String comment) {
      Delete delete = (new Delete()).setTableName(tableName).setWhere("(" + StringHelper.join(", ", columnNames) + ") IN (" + idSubselect + ")");
      if (this.factory().getSettings().isCommentsEnabled()) {
         delete.setComment(comment);
      }

      return delete.toStatementString();
   }

   public Queryable getTargetedQueryable() {
      return this.targetedPersister;
   }

   public String[] getSqlStatements() {
      return (String[])this.deletes.toArray(new String[this.deletes.size()]);
   }

   public int execute(SessionImplementor session, QueryParameters queryParameters) {
      this.prepareForUse(this.targetedPersister, session);

      int pos;
      try {
         PreparedStatement ps = null;
         int resultCount = 0;

         try {
            try {
               ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.idInsertSelect, false);
               pos = 1;
               pos += this.handlePrependedParametersOnIdSelection(ps, session, pos);

               for(ParameterSpecification parameterSpecification : this.idSelectParameterSpecifications) {
                  pos += parameterSpecification.bind(ps, queryParameters, session, pos);
               }

               resultCount = ps.executeUpdate();
            } finally {
               if (ps != null) {
                  ps.close();
               }

            }
         } catch (SQLException e) {
            throw this.convert(e, "could not insert/select ids for bulk delete", this.idInsertSelect);
         }

         for(String delete : this.deletes) {
            try {
               try {
                  ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(delete, false);
                  this.handleAddedParametersOnDelete(ps, session);
                  ps.executeUpdate();
               } finally {
                  if (ps != null) {
                     ps.close();
                  }

               }
            } catch (SQLException e) {
               throw this.convert(e, "error performing bulk delete", delete);
            }
         }

         pos = resultCount;
      } finally {
         this.releaseFromUse(this.targetedPersister, session);
      }

      return pos;
   }

   protected int handlePrependedParametersOnIdSelection(PreparedStatement ps, SessionImplementor session, int pos) throws SQLException {
      return 0;
   }

   protected void handleAddedParametersOnDelete(PreparedStatement ps, SessionImplementor session) throws SQLException {
   }
}
