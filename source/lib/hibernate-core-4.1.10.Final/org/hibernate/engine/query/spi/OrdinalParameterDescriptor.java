package org.hibernate.engine.query.spi;

import java.io.Serializable;
import org.hibernate.type.Type;

public class OrdinalParameterDescriptor implements Serializable {
   private final int ordinalPosition;
   private final Type expectedType;
   private final int sourceLocation;

   public OrdinalParameterDescriptor(int ordinalPosition, Type expectedType, int sourceLocation) {
      this.ordinalPosition = ordinalPosition;
      this.expectedType = expectedType;
      this.sourceLocation = sourceLocation;
   }

   public int getOrdinalPosition() {
      return this.ordinalPosition;
   }

   public Type getExpectedType() {
      return this.expectedType;
   }

   public int getSourceLocation() {
      return this.sourceLocation;
   }
}
