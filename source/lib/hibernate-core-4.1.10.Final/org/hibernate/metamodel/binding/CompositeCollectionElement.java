package org.hibernate.metamodel.binding;

public class CompositeCollectionElement extends AbstractCollectionElement {
   public CompositeCollectionElement(AbstractPluralAttributeBinding binding) {
      super(binding);
   }

   public CollectionElementNature getCollectionElementNature() {
      return CollectionElementNature.COMPOSITE;
   }
}
