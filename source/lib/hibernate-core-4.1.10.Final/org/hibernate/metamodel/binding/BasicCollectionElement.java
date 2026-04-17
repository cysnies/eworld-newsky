package org.hibernate.metamodel.binding;

public class BasicCollectionElement extends AbstractCollectionElement {
   private final HibernateTypeDescriptor hibernateTypeDescriptor = new HibernateTypeDescriptor();

   public BasicCollectionElement(AbstractPluralAttributeBinding binding) {
      super(binding);
   }

   public CollectionElementNature getCollectionElementNature() {
      return CollectionElementNature.BASIC;
   }

   public HibernateTypeDescriptor getHibernateTypeDescriptor() {
      return this.hibernateTypeDescriptor;
   }
}
