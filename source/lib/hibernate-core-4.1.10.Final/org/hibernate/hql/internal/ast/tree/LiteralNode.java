package org.hibernate.hql.internal.ast.tree;

import antlr.SemanticException;
import org.hibernate.hql.internal.antlr.HqlSqlTokenTypes;
import org.hibernate.hql.internal.ast.util.ColumnHelper;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class LiteralNode extends AbstractSelectExpression implements HqlSqlTokenTypes {
   public void setScalarColumnText(int i) throws SemanticException {
      ColumnHelper.generateSingleScalarColumn(this, i);
   }

   public Type getDataType() {
      switch (this.getType()) {
         case 20:
         case 49:
            return StandardBasicTypes.BOOLEAN;
         case 95:
            return StandardBasicTypes.DOUBLE;
         case 96:
            return StandardBasicTypes.FLOAT;
         case 97:
            return StandardBasicTypes.LONG;
         case 98:
            return StandardBasicTypes.BIG_INTEGER;
         case 99:
            return StandardBasicTypes.BIG_DECIMAL;
         case 124:
            return StandardBasicTypes.INTEGER;
         case 125:
            return StandardBasicTypes.STRING;
         default:
            return null;
      }
   }
}
