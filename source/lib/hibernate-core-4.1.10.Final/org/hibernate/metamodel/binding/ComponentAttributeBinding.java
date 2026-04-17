package org.hibernate.metamodel.binding;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.domain.AttributeContainer;
import org.hibernate.metamodel.domain.Component;
import org.hibernate.metamodel.domain.PluralAttribute;
import org.hibernate.metamodel.domain.PluralAttributeNature;
import org.hibernate.metamodel.domain.SingularAttribute;
import org.hibernate.metamodel.source.MetaAttributeContext;

public class ComponentAttributeBinding extends AbstractSingularAttributeBinding implements AttributeBindingContainer {
   private final String path;
   private Map attributeBindingMap = new HashMap();
   private SingularAttribute parentReference;
   private MetaAttributeContext metaAttributeContext;

   public ComponentAttributeBinding(AttributeBindingContainer container, SingularAttribute attribute) {
      super(container, attribute);
      this.path = container.getPathBase() + '.' + attribute.getName();
   }

   public EntityBinding seekEntityBinding() {
      return this.getContainer().seekEntityBinding();
   }

   public String getPathBase() {
      return this.path;
   }

   public AttributeContainer getAttributeContainer() {
      return this.getComponent();
   }

   public Component getComponent() {
      return (Component)this.getAttribute().getSingularAttributeType();
   }

   public boolean isAssociation() {
      return false;
   }

   public MetaAttributeContext getMetaAttributeContext() {
      return this.metaAttributeContext;
   }

   public void setMetaAttributeContext(MetaAttributeContext metaAttributeContext) {
      this.metaAttributeContext = metaAttributeContext;
   }

   public AttributeBinding locateAttributeBinding(String name) {
      return (AttributeBinding)this.attributeBindingMap.get(name);
   }

   public Iterable attributeBindings() {
      return this.attributeBindingMap.values();
   }

   protected void checkValueBinding() {
   }

   public BasicAttributeBinding makeBasicAttributeBinding(SingularAttribute attribute) {
      BasicAttributeBinding binding = new BasicAttributeBinding(this, attribute, this.isNullable(), this.isAlternateUniqueKey());
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   protected void registerAttributeBinding(String name, AttributeBinding attributeBinding) {
      this.attributeBindingMap.put(name, attributeBinding);
   }

   public ComponentAttributeBinding makeComponentAttributeBinding(SingularAttribute attribute) {
      ComponentAttributeBinding binding = new ComponentAttributeBinding(this, attribute);
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   public ManyToOneAttributeBinding makeManyToOneAttributeBinding(SingularAttribute attribute) {
      ManyToOneAttributeBinding binding = new ManyToOneAttributeBinding(this, attribute);
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   public BagBinding makeBagAttributeBinding(PluralAttribute attribute, CollectionElementNature nature) {
      Helper.checkPluralAttributeNature(attribute, PluralAttributeNature.BAG);
      BagBinding binding = new BagBinding(this, attribute, nature);
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   public SetBinding makeSetAttributeBinding(PluralAttribute attribute, CollectionElementNature nature) {
      Helper.checkPluralAttributeNature(attribute, PluralAttributeNature.SET);
      SetBinding binding = new SetBinding(this, attribute, nature);
      this.registerAttributeBinding(attribute.getName(), binding);
      return binding;
   }

   public Class getClassReference() {
      return this.getComponent().getClassReference();
   }

   public SingularAttribute getParentReference() {
      return this.parentReference;
   }

   public void setParentReference(SingularAttribute parentReference) {
      this.parentReference = parentReference;
   }

   public PropertyGeneration getGeneration() {
      return null;
   }
}
