package org.hibernate.metamodel.domain;

import org.hibernate.internal.util.ValueHolder;

public class NonEntity extends AbstractAttributeContainer {
   public NonEntity(String entityName, String className, ValueHolder classReference, Hierarchical superType) {
      super(entityName, className, classReference, superType);
   }

   public boolean isAssociation() {
      return true;
   }

   public boolean isComponent() {
      return false;
   }
}
