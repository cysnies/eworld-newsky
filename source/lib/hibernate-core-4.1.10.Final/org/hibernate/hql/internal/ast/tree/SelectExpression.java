package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import org.hibernate.type.Type;

public interface SelectExpression {
   Type getDataType();

   void setScalarColumnText(int var1) throws SemanticException;

   void setScalarColumn(int var1) throws SemanticException;

   int getScalarColumnIndex();

   FromElement getFromElement();

   boolean isConstructor();

   boolean isReturnableEntity() throws SemanticException;

   void setText(String var1);

   boolean isScalar() throws SemanticException;

   void setAlias(String var1);

   String getAlias();
}
