package org.hibernate.metamodel.binding;

import org.hibernate.metamodel.domain.AttributeContainer;
import org.hibernate.metamodel.domain.PluralAttribute;
import org.hibernate.metamodel.domain.SingularAttribute;
import org.hibernate.metamodel.source.MetaAttributeContext;

public interface AttributeBindingContainer {
   String getPathBase();

   AttributeContainer getAttributeContainer();

   Iterable attributeBindings();

   AttributeBinding locateAttributeBinding(String var1);

   BasicAttributeBinding makeBasicAttributeBinding(SingularAttribute var1);

   ComponentAttributeBinding makeComponentAttributeBinding(SingularAttribute var1);

   ManyToOneAttributeBinding makeManyToOneAttributeBinding(SingularAttribute var1);

   BagBinding makeBagAttributeBinding(PluralAttribute var1, CollectionElementNature var2);

   SetBinding makeSetAttributeBinding(PluralAttribute var1, CollectionElementNature var2);

   EntityBinding seekEntityBinding();

   Class getClassReference();

   MetaAttributeContext getMetaAttributeContext();
}
