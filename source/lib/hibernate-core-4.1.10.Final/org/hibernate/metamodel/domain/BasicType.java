package org.hibernate.metamodel.domain;

import org.hibernate.internal.util.ValueHolder;

public class BasicType implements Type {
   private final String name;
   private final ValueHolder classReference;

   public BasicType(String name, ValueHolder classReference) {
      this.name = name;
      this.classReference = classReference;
   }

   public String getName() {
      return this.name;
   }

   public String getClassName() {
      return this.name;
   }

   public Class getClassReference() {
      return (Class)this.classReference.getValue();
   }

   public ValueHolder getClassReferenceUnresolved() {
      return this.classReference;
   }

   public boolean isAssociation() {
      return false;
   }

   public boolean isComponent() {
      return false;
   }
}
