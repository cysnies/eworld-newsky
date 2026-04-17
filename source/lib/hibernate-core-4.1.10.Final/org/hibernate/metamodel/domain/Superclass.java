package org.hibernate.metamodel.domain;

import org.hibernate.internal.util.ValueHolder;

public class Superclass extends AbstractAttributeContainer {
   public Superclass(String entityName, String className, ValueHolder classReference, Hierarchical superType) {
      super(entityName, className, classReference, superType);
   }

   public boolean isAssociation() {
      return true;
   }

   public boolean isComponent() {
      return false;
   }
}
