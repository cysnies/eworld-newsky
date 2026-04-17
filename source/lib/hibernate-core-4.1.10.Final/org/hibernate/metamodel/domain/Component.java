package org.hibernate.metamodel.domain;

import org.hibernate.internal.util.ValueHolder;

public class Component extends AbstractAttributeContainer {
   public Component(String name, String className, ValueHolder classReference, Hierarchical superType) {
      super(name, className, classReference, superType);
   }

   public boolean isAssociation() {
      return false;
   }

   public boolean isComponent() {
      return true;
   }

   public String getRoleBaseName() {
      return this.getClassName();
   }
}
