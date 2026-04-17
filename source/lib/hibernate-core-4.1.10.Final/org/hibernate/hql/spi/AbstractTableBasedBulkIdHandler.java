package org.hibernate.hql.spi;

import antlr.RecognitionException;
import antlr.collections.AST;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.hql.internal.ast.SqlGenerator;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Table;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.InsertSelect;
import org.hibernate.sql.Select;
import org.hibernate.sql.SelectValues;

public class AbstractTableBasedBulkIdHandler {
   private final SessionFactoryImplementor sessionFactory;
   private final HqlSqlWalker walker;
   private final String catalog;
   private final String schema;

   public AbstractTableBasedBulkIdHandler(SessionFactoryImplementor sessionFactory, HqlSqlWalker walker, String catalog, String schema) {
      this.sessionFactory = sessionFactory;
      this.walker = walker;
      this.catalog = catalog;
      this.schema = schema;
   }

   protected SessionFactoryImplementor factory() {
      return this.sessionFactory;
   }

   protected HqlSqlWalker walker() {
      return this.walker;
   }

   protected JDBCException convert(SQLException e, String message, String sql) {
      throw this.factory().getSQLExceptionHelper().convert(e, message, sql);
   }

   protected ProcessedWhereClause processWhereClause(AST whereClause) {
      if (whereClause.getNumberOfChildren() != 0) {
         try {
            SqlGenerator sqlGenerator = new SqlGenerator(this.sessionFactory);
            sqlGenerator.whereClause(whereClause);
            String userWhereClause = sqlGenerator.getSQL().substring(7);
            List<ParameterSpecification> idSelectParameterSpecifications = sqlGenerator.getCollectedParameters();
            return new ProcessedWhereClause(userWhereClause, idSelectParameterSpecifications);
         } catch (RecognitionException e) {
            throw new HibernateException("Unable to generate id select for DML operation", e);
         }
      } else {
         return AbstractTableBasedBulkIdHandler.ProcessedWhereClause.NO_WHERE_CLAUSE;
      }
   }

   protected String generateIdInsertSelect(Queryable persister, String tableAlias, ProcessedWhereClause whereClause) {
      Select select = new Select(this.sessionFactory.getDialect());
      SelectValues selectClause = (new SelectValues(this.sessionFactory.getDialect())).addColumns(tableAlias, persister.getIdentifierColumnNames(), persister.getIdentifierColumnNames());
      this.addAnyExtraIdSelectValues(selectClause);
      select.setSelectClause(selectClause.render());
      String rootTableName = persister.getTableName();
      String fromJoinFragment = persister.fromJoinFragment(tableAlias, true, false);
      String whereJoinFragment = persister.whereJoinFragment(tableAlias, true, false);
      select.setFromClause(rootTableName + ' ' + tableAlias + fromJoinFragment);
      if (whereJoinFragment == null) {
         whereJoinFragment = "";
      } else {
         whereJoinFragment = whereJoinFragment.trim();
         if (whereJoinFragment.startsWith("and")) {
            whereJoinFragment = whereJoinFragment.substring(4);
         }
      }

      if (whereClause.getUserWhereClauseFragment().length() > 0 && whereJoinFragment.length() > 0) {
         whereJoinFragment = whereJoinFragment + " and ";
      }

      select.setWhereClause(whereJoinFragment + whereClause.getUserWhereClauseFragment());
      InsertSelect insert = new InsertSelect(this.sessionFactory.getDialect());
      if (this.sessionFactory.getSettings().isCommentsEnabled()) {
         insert.setComment("insert-select for " + persister.getEntityName() + " ids");
      }

      insert.setTableName(this.determineIdTableName(persister));
      insert.setSelect(select);
      return insert.toStatementString();
   }

   protected void addAnyExtraIdSelectValues(SelectValues selectClause) {
   }

   protected String determineIdTableName(Queryable persister) {
      return Table.qualify(this.catalog, this.schema, persister.getTemporaryIdTableName());
   }

   protected String generateIdSubselect(Queryable persister) {
      return "select " + StringHelper.join(", ", persister.getIdentifierColumnNames()) + " from " + this.determineIdTableName(persister);
   }

   protected void prepareForUse(Queryable persister, SessionImplementor session) {
   }

   protected void releaseFromUse(Queryable persister, SessionImplementor session) {
   }

   protected static class ProcessedWhereClause {
      public static final ProcessedWhereClause NO_WHERE_CLAUSE = new ProcessedWhereClause();
      private final String userWhereClauseFragment;
      private final List idSelectParameterSpecifications;

      private ProcessedWhereClause() {
         this("", Collections.emptyList());
      }

      public ProcessedWhereClause(String userWhereClauseFragment, List idSelectParameterSpecifications) {
         this.userWhereClauseFragment = userWhereClauseFragment;
         this.idSelectParameterSpecifications = idSelectParameterSpecifications;
      }

      public String getUserWhereClauseFragment() {
         return this.userWhereClauseFragment;
      }

      public List getIdSelectParameterSpecifications() {
         return this.idSelectParameterSpecifications;
      }
   }
}
