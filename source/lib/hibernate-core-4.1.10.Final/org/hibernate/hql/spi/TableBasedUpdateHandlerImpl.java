package org.hibernate.hql.spi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.tree.AssignmentSpecification;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.UpdateStatement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.Update;
import org.jboss.logging.Logger;

public class TableBasedUpdateHandlerImpl extends AbstractTableBasedBulkIdHandler implements MultiTableBulkIdStrategy.UpdateHandler {
   private static final Logger log = Logger.getLogger(TableBasedUpdateHandlerImpl.class);
   private final Queryable targetedPersister;
   private final String idInsertSelect;
   private final List idSelectParameterSpecifications;
   private final String[] updates;
   private final ParameterSpecification[][] assignmentParameterSpecifications;

   public TableBasedUpdateHandlerImpl(SessionFactoryImplementor factory, HqlSqlWalker walker) {
      this(factory, walker, (String)null, (String)null);
   }

   public TableBasedUpdateHandlerImpl(SessionFactoryImplementor factory, HqlSqlWalker walker, String catalog, String schema) {
      super(factory, walker, catalog, schema);
      UpdateStatement updateStatement = (UpdateStatement)walker.getAST();
      FromElement fromElement = updateStatement.getFromClause().getFromElement();
      this.targetedPersister = fromElement.getQueryable();
      String bulkTargetAlias = fromElement.getTableAlias();
      AbstractTableBasedBulkIdHandler.ProcessedWhereClause processedWhereClause = this.processWhereClause(updateStatement.getWhereClause());
      this.idSelectParameterSpecifications = processedWhereClause.getIdSelectParameterSpecifications();
      this.idInsertSelect = this.generateIdInsertSelect(this.targetedPersister, bulkTargetAlias, processedWhereClause);
      log.tracev("Generated ID-INSERT-SELECT SQL (multi-table update) : {0}", this.idInsertSelect);
      String[] tableNames = this.targetedPersister.getConstraintOrderedTableNameClosure();
      String[][] columnNames = this.targetedPersister.getContraintOrderedTableKeyColumnClosure();
      String idSubselect = this.generateIdSubselect(this.targetedPersister);
      this.updates = new String[tableNames.length];
      this.assignmentParameterSpecifications = new ParameterSpecification[tableNames.length][];

      for(int tableIndex = 0; tableIndex < tableNames.length; ++tableIndex) {
         boolean affected = false;
         List<ParameterSpecification> parameterList = new ArrayList();
         Update update = (new Update(this.factory().getDialect())).setTableName(tableNames[tableIndex]).setWhere("(" + StringHelper.join(", ", columnNames[tableIndex]) + ") IN (" + idSubselect + ")");
         if (this.factory().getSettings().isCommentsEnabled()) {
            update.setComment("bulk update");
         }

         for(AssignmentSpecification assignmentSpecification : walker.getAssignmentSpecifications()) {
            if (assignmentSpecification.affectsTable(tableNames[tableIndex])) {
               affected = true;
               update.appendAssignmentFragment(assignmentSpecification.getSqlAssignmentFragment());
               if (assignmentSpecification.getParameters() != null) {
                  for(int paramIndex = 0; paramIndex < assignmentSpecification.getParameters().length; ++paramIndex) {
                     parameterList.add(assignmentSpecification.getParameters()[paramIndex]);
                  }
               }
            }
         }

         if (affected) {
            this.updates[tableIndex] = update.toStatementString();
            this.assignmentParameterSpecifications[tableIndex] = (ParameterSpecification[])parameterList.toArray(new ParameterSpecification[parameterList.size()]);
         }
      }

   }

   public Queryable getTargetedQueryable() {
      return this.targetedPersister;
   }

   public String[] getSqlStatements() {
      return this.updates;
   }

   public int execute(SessionImplementor session, QueryParameters queryParameters) {
      this.prepareForUse(this.targetedPersister, session);

      int sum;
      try {
         PreparedStatement ps = null;
         int resultCount = 0;

         try {
            try {
               ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.idInsertSelect, false);
               sum = 1;
               sum += this.handlePrependedParametersOnIdSelection(ps, session, sum);

               for(ParameterSpecification parameterSpecification : this.idSelectParameterSpecifications) {
                  sum += parameterSpecification.bind(ps, queryParameters, session, sum);
               }

               resultCount = ps.executeUpdate();
            } finally {
               if (ps != null) {
                  ps.close();
               }

            }
         } catch (SQLException e) {
            throw this.convert(e, "could not insert/select ids for bulk update", this.idInsertSelect);
         }

         for(int i = 0; i < this.updates.length; ++i) {
            if (this.updates[i] != null) {
               try {
                  try {
                     ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(this.updates[i], false);
                     if (this.assignmentParameterSpecifications[i] != null) {
                        int position = 1;

                        for(int x = 0; x < this.assignmentParameterSpecifications[i].length; ++x) {
                           position += this.assignmentParameterSpecifications[i][x].bind(ps, queryParameters, session, position);
                        }

                        this.handleAddedParametersOnUpdate(ps, session, position);
                     }

                     ps.executeUpdate();
                  } finally {
                     if (ps != null) {
                        ps.close();
                     }

                  }
               } catch (SQLException e) {
                  throw this.convert(e, "error performing bulk update", this.updates[i]);
               }
            }
         }

         sum = resultCount;
      } finally {
         this.releaseFromUse(this.targetedPersister, session);
      }

      return sum;
   }

   protected int handlePrependedParametersOnIdSelection(PreparedStatement ps, SessionImplementor session, int pos) throws SQLException {
      return 0;
   }

   protected void handleAddedParametersOnUpdate(PreparedStatement ps, SessionImplementor session, int position) throws SQLException {
   }
}
