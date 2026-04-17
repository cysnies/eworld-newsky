package org.hibernate.param;

import org.hibernate.type.Type;

public abstract class AbstractExplicitParameterSpecification implements ExplicitParameterSpecification {
   private final int sourceLine;
   private final int sourceColumn;
   private Type expectedType;

   protected AbstractExplicitParameterSpecification(int sourceLine, int sourceColumn) {
      this.sourceLine = sourceLine;
      this.sourceColumn = sourceColumn;
   }

   public int getSourceLine() {
      return this.sourceLine;
   }

   public int getSourceColumn() {
      return this.sourceColumn;
   }

   public Type getExpectedType() {
      return this.expectedType;
   }

   public void setExpectedType(Type expectedType) {
      this.expectedType = expectedType;
   }
}
