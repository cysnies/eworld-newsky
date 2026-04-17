package org.hibernate.metamodel.binding;

import org.hibernate.metamodel.relational.Value;

public abstract class AbstractCollectionElement {
   private final AbstractPluralAttributeBinding collectionBinding;
   private Value elementValue;

   AbstractCollectionElement(AbstractPluralAttributeBinding collectionBinding) {
      this.collectionBinding = collectionBinding;
   }

   public abstract CollectionElementNature getCollectionElementNature();

   public AbstractPluralAttributeBinding getCollectionBinding() {
      return this.collectionBinding;
   }

   public Value getElementValue() {
      return this.elementValue;
   }
}
