package org.hibernate.metamodel.binding;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.metamodel.domain.Attribute;
import org.hibernate.metamodel.source.MetaAttributeContext;

public abstract class AbstractAttributeBinding implements AttributeBinding {
   private final AttributeBindingContainer container;
   private final Attribute attribute;
   private final HibernateTypeDescriptor hibernateTypeDescriptor = new HibernateTypeDescriptor();
   private final Set entityReferencingAttributeBindings = new HashSet();
   private boolean includedInOptimisticLocking;
   private boolean isLazy;
   private String propertyAccessorName;
   private boolean isAlternateUniqueKey;
   private MetaAttributeContext metaAttributeContext;

   protected AbstractAttributeBinding(AttributeBindingContainer container, Attribute attribute) {
      this.container = container;
      this.attribute = attribute;
   }

   public AttributeBindingContainer getContainer() {
      return this.container;
   }

   public Attribute getAttribute() {
      return this.attribute;
   }

   public HibernateTypeDescriptor getHibernateTypeDescriptor() {
      return this.hibernateTypeDescriptor;
   }

   public boolean isBasicPropertyAccessor() {
      return this.propertyAccessorName == null || "property".equals(this.propertyAccessorName);
   }

   public String getPropertyAccessorName() {
      return this.propertyAccessorName;
   }

   public void setPropertyAccessorName(String propertyAccessorName) {
      this.propertyAccessorName = propertyAccessorName;
   }

   public boolean isIncludedInOptimisticLocking() {
      return this.includedInOptimisticLocking;
   }

   public void setIncludedInOptimisticLocking(boolean includedInOptimisticLocking) {
      this.includedInOptimisticLocking = includedInOptimisticLocking;
   }

   public MetaAttributeContext getMetaAttributeContext() {
      return this.metaAttributeContext;
   }

   public void setMetaAttributeContext(MetaAttributeContext metaAttributeContext) {
      this.metaAttributeContext = metaAttributeContext;
   }

   public boolean isAlternateUniqueKey() {
      return this.isAlternateUniqueKey;
   }

   public void setAlternateUniqueKey(boolean alternateUniqueKey) {
      this.isAlternateUniqueKey = alternateUniqueKey;
   }

   public boolean isLazy() {
      return this.isLazy;
   }

   public void setLazy(boolean isLazy) {
      this.isLazy = isLazy;
   }

   public void addEntityReferencingAttributeBinding(SingularAssociationAttributeBinding referencingAttributeBinding) {
      this.entityReferencingAttributeBindings.add(referencingAttributeBinding);
   }

   public Set getEntityReferencingAttributeBindings() {
      return Collections.unmodifiableSet(this.entityReferencingAttributeBindings);
   }

   public void validate() {
      if (!this.entityReferencingAttributeBindings.isEmpty()) {
      }

   }
}
