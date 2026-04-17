package org.hibernate.metamodel.binding;

public class ManyToAnyCollectionElement extends AbstractCollectionElement {
   ManyToAnyCollectionElement(AbstractPluralAttributeBinding binding) {
      super(binding);
   }

   public CollectionElementNature getCollectionElementNature() {
      return CollectionElementNature.MANY_TO_ANY;
   }
}
