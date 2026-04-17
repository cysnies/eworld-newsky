package org.hibernate.metamodel.binding;

import java.util.Set;
import org.hibernate.metamodel.domain.Attribute;
import org.hibernate.metamodel.source.MetaAttributeContext;

public interface AttributeBinding {
   AttributeBindingContainer getContainer();

   Attribute getAttribute();

   HibernateTypeDescriptor getHibernateTypeDescriptor();

   boolean isAssociation();

   boolean isBasicPropertyAccessor();

   String getPropertyAccessorName();

   void setPropertyAccessorName(String var1);

   boolean isIncludedInOptimisticLocking();

   void setIncludedInOptimisticLocking(boolean var1);

   MetaAttributeContext getMetaAttributeContext();

   boolean isAlternateUniqueKey();

   boolean isLazy();

   void addEntityReferencingAttributeBinding(SingularAssociationAttributeBinding var1);

   Set getEntityReferencingAttributeBindings();

   void validate();
}
