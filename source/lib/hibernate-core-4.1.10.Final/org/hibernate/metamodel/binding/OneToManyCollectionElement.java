package org.hibernate.metamodel.binding;

public class OneToManyCollectionElement extends AbstractCollectionElement {
   OneToManyCollectionElement(AbstractPluralAttributeBinding binding) {
      super(binding);
   }

   public CollectionElementNature getCollectionElementNature() {
      return CollectionElementNature.ONE_TO_MANY;
   }
}
