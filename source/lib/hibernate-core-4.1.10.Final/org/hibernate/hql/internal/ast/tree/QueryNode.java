package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import antlr.collections.AST;
import org.hibernate.hql.internal.ast.util.ASTUtil;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class QueryNode extends AbstractRestrictableStatement implements SelectExpression {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, QueryNode.class.getName());
   private OrderByClause orderByClause;
   private int scalarColumnIndex = -1;
   private String alias;

   public int getStatementType() {
      return 86;
   }

   public boolean needsExecutor() {
      return false;
   }

   protected int getWhereClauseParentTokenType() {
      return 22;
   }

   protected CoreMessageLogger getLog() {
      return LOG;
   }

   public final SelectClause getSelectClause() {
      return (SelectClause)ASTUtil.findTypeInChildren(this, 137);
   }

   public final boolean hasOrderByClause() {
      OrderByClause orderByClause = this.locateOrderByClause();
      return orderByClause != null && orderByClause.getNumberOfChildren() > 0;
   }

   public final OrderByClause getOrderByClause() {
      if (this.orderByClause == null) {
         this.orderByClause = this.locateOrderByClause();
         if (this.orderByClause == null) {
            LOG.debug("getOrderByClause() : Creating a new ORDER BY clause");
            this.orderByClause = (OrderByClause)ASTUtil.create(this.getWalker().getASTFactory(), 41, "ORDER");
            AST prevSibling = ASTUtil.findTypeInChildren(this, 53);
            if (prevSibling == null) {
               prevSibling = ASTUtil.findTypeInChildren(this, 22);
            }

            this.orderByClause.setNextSibling(prevSibling.getNextSibling());
            prevSibling.setNextSibling(this.orderByClause);
         }
      }

      return this.orderByClause;
   }

   private OrderByClause locateOrderByClause() {
      return (OrderByClause)ASTUtil.findTypeInChildren(this, 41);
   }

   public String getAlias() {
      return this.alias;
   }

   public FromElement getFromElement() {
      return null;
   }

   public boolean isConstructor() {
      return false;
   }

   public boolean isReturnableEntity() throws SemanticException {
      return false;
   }

   public boolean isScalar() throws SemanticException {
      return true;
   }

   public void setAlias(String alias) {
      this.alias = alias;
   }

   public void setScalarColumn(int i) throws SemanticException {
      this.scalarColumnIndex = i;
      this.setScalarColumnText(i);
   }

   public int getScalarColumnIndex() {
      return this.scalarColumnIndex;
   }

   public void setScalarColumnText(int i) throws SemanticException {
      ColumnHelper.generateSingleScalarColumn(this, i);
   }

   public Type getDataType() {
      return ((SelectExpression)this.getSelectClause().getFirstSelectExpression()).getDataType();
   }
}
