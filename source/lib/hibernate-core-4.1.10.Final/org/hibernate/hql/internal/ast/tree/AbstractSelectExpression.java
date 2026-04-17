package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import org.hibernate.type.Type;

public abstract class AbstractSelectExpression extends HqlSqlWalkerNode implements SelectExpression {
   private String alias;
   private int scalarColumnIndex = -1;

   public final void setAlias(String alias) {
      this.alias = alias;
   }

   public final String getAlias() {
      return this.alias;
   }

   public boolean isConstructor() {
      return false;
   }

   public boolean isReturnableEntity() throws SemanticException {
      return false;
   }

   public FromElement getFromElement() {
      return null;
   }

   public boolean isScalar() throws SemanticException {
      Type type = this.getDataType();
      return type != null && !type.isAssociationType();
   }

   public void setScalarColumn(int i) throws SemanticException {
      this.scalarColumnIndex = i;
      this.setScalarColumnText(i);
   }

   public int getScalarColumnIndex() {
      return this.scalarColumnIndex;
   }
}
