package org.hibernate.sql.ordering.antlr;

import antlr.ASTFactory;

public class Factory extends ASTFactory implements OrderByTemplateTokenTypes {
   public Class getASTNodeType(int i) {
      switch (i) {
         case 4:
            return OrderByFragment.class;
         case 5:
            return SortSpecification.class;
         case 6:
            return OrderingSpecification.class;
         case 7:
            return SortKey.class;
         case 8:
         case 9:
         case 10:
         case 11:
         default:
            return NodeSupport.class;
         case 12:
            return CollationSpecification.class;
      }
   }
}
