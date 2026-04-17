package org.hibernate.engine.query.spi;

import java.io.Serializable;
import org.hibernate.type.Type;

public class NamedParameterDescriptor implements Serializable {
   private final String name;
   private Type expectedType;
   private final int[] sourceLocations;
   private final boolean jpaStyle;

   public NamedParameterDescriptor(String name, Type expectedType, int[] sourceLocations, boolean jpaStyle) {
      this.name = name;
      this.expectedType = expectedType;
      this.sourceLocations = sourceLocations;
      this.jpaStyle = jpaStyle;
   }

   public String getName() {
      return this.name;
   }

   public Type getExpectedType() {
      return this.expectedType;
   }

   public int[] getSourceLocations() {
      return this.sourceLocations;
   }

   public boolean isJpaStyle() {
      return this.jpaStyle;
   }

   public void resetExpectedType(Type type) {
      this.expectedType = type;
   }
}
