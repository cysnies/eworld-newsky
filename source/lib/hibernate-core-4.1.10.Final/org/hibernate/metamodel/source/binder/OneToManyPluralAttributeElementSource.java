package org.hibernate.metamodel.source.binder;

public interface OneToManyPluralAttributeElementSource extends PluralAttributeElementSource {
   String getReferencedEntityName();

   boolean isNotFoundAnException();
}
