package org.hibernate.sql.ordering.antlr;

import antlr.collections.AST;

public class SortSpecification extends NodeSupport {
   public SortKey getSortKey() {
      return (SortKey)this.getFirstChild();
   }

   public CollationSpecification getCollation() {
      AST possible = this.getSortKey().getNextSibling();
      return possible != null && 12 == possible.getType() ? (CollationSpecification)possible : null;
   }

   public OrderingSpecification getOrdering() {
      AST possible = this.getSortKey().getNextSibling();
      if (possible == null) {
         return null;
      } else {
         if (12 == possible.getType()) {
            possible = possible.getNextSibling();
         }

         return possible != null && 6 == possible.getType() ? (OrderingSpecification)possible : null;
      }
   }
}
