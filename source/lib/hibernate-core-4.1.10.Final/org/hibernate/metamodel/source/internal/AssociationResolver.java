package org.hibernate.metamodel.source.internal;

import org.hibernate.MappingException;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.SingularAssociationAttributeBinding;
import org.hibernate.metamodel.source.MetadataImplementor;

class AssociationResolver {
   private final MetadataImplementor metadata;

   AssociationResolver(MetadataImplementor metadata) {
      this.metadata = metadata;
   }

   void resolve() {
      for(EntityBinding entityBinding : this.metadata.getEntityBindings()) {
         for(SingularAssociationAttributeBinding attributeBinding : entityBinding.getEntityReferencingAttributeBindings()) {
            this.resolve(attributeBinding);
         }
      }

   }

   private void resolve(SingularAssociationAttributeBinding attributeBinding) {
      if (attributeBinding.getReferencedEntityName() == null) {
         throw new IllegalArgumentException("attributeBinding has null entityName: " + attributeBinding.getAttribute().getName());
      } else {
         EntityBinding entityBinding = this.metadata.getEntityBinding(attributeBinding.getReferencedEntityName());
         if (entityBinding == null) {
            throw new MappingException(String.format("Attribute [%s] refers to unknown entity: [%s]", attributeBinding.getAttribute().getName(), attributeBinding.getReferencedEntityName()));
         } else {
            AttributeBinding referencedAttributeBinding = (AttributeBinding)(attributeBinding.isPropertyReference() ? entityBinding.locateAttributeBinding(attributeBinding.getReferencedAttributeName()) : entityBinding.getHierarchyDetails().getEntityIdentifier().getValueBinding());
            if (referencedAttributeBinding == null) {
               throw new MappingException(String.format("Attribute [%s] refers to unknown attribute: [%s]", attributeBinding.getAttribute().getName(), attributeBinding.getReferencedEntityName()));
            } else {
               attributeBinding.resolveReference(referencedAttributeBinding);
               referencedAttributeBinding.addEntityReferencingAttributeBinding(attributeBinding);
            }
         }
      }
   }
}
