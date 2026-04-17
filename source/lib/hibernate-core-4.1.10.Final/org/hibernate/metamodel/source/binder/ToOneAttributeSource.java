package org.hibernate.metamodel.source.binder;

public interface ToOneAttributeSource extends SingularAttributeSource, AssociationAttributeSource {
   String getReferencedEntityName();

   String getReferencedEntityAttributeName();
}
