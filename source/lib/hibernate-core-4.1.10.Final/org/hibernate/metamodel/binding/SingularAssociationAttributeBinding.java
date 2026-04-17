package org.hibernate.metamodel.binding;

public interface SingularAssociationAttributeBinding extends SingularAttributeBinding, AssociationAttributeBinding {
   boolean isPropertyReference();

   String getReferencedEntityName();

   void setReferencedEntityName(String var1);

   String getReferencedAttributeName();

   void setReferencedAttributeName(String var1);

   void resolveReference(AttributeBinding var1);

   boolean isReferenceResolved();

   EntityBinding getReferencedEntityBinding();

   AttributeBinding getReferencedAttributeBinding();
}
