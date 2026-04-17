package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;

public class ResultVariableRefNode extends HqlSqlWalkerNode {
   private SelectExpression selectExpression;

   public void setSelectExpression(SelectExpression selectExpression) throws SemanticException {
      if (selectExpression != null && selectExpression.getAlias() != null) {
         this.selectExpression = selectExpression;
      } else {
         throw new SemanticException("A ResultVariableRefNode must refer to a non-null alias.");
      }
   }

   public String getRenderText(SessionFactoryImplementor sessionFactory) {
      int scalarColumnIndex = this.selectExpression.getScalarColumnIndex();
      if (scalarColumnIndex < 0) {
         throw new IllegalStateException("selectExpression.getScalarColumnIndex() must be >= 0; actual = " + scalarColumnIndex);
      } else {
         return sessionFactory.getDialect().replaceResultVariableInOrderByClauseWithPosition() ? this.getColumnPositionsString(scalarColumnIndex) : this.getColumnNamesString(scalarColumnIndex);
      }
   }

   private String getColumnPositionsString(int scalarColumnIndex) {
      int startPosition = this.getWalker().getSelectClause().getColumnNamesStartPosition(scalarColumnIndex);
      StringBuilder buf = new StringBuilder();
      int nColumns = this.getWalker().getSelectClause().getColumnNames()[scalarColumnIndex].length;

      for(int i = startPosition; i < startPosition + nColumns; ++i) {
         if (i > startPosition) {
            buf.append(", ");
         }

         buf.append(i);
      }

      return buf.toString();
   }

   private String getColumnNamesString(int scalarColumnIndex) {
      return StringHelper.join(", ", this.getWalker().getSelectClause().getColumnNames()[scalarColumnIndex]);
   }
}
