package org.hibernate.metamodel.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.ValueHolder;

public abstract class AbstractAttributeContainer implements AttributeContainer, Hierarchical {
   private final String name;
   private final String className;
   private final ValueHolder classReference;
   private final Hierarchical superType;
   private LinkedHashSet attributeSet = new LinkedHashSet();
   private HashMap attributeMap = new HashMap();

   public AbstractAttributeContainer(String name, String className, ValueHolder classReference, Hierarchical superType) {
      this.name = name;
      this.className = className;
      this.classReference = classReference;
      this.superType = superType;
   }

   public String getName() {
      return this.name;
   }

   public String getClassName() {
      return this.className;
   }

   public Class getClassReference() {
      return (Class)this.classReference.getValue();
   }

   public ValueHolder getClassReferenceUnresolved() {
      return this.classReference;
   }

   public Hierarchical getSuperType() {
      return this.superType;
   }

   public Set attributes() {
      return Collections.unmodifiableSet(this.attributeSet);
   }

   public String getRoleBaseName() {
      return this.getClassName();
   }

   public Attribute locateAttribute(String name) {
      return (Attribute)this.attributeMap.get(name);
   }

   public SingularAttribute locateSingularAttribute(String name) {
      return (SingularAttribute)this.locateAttribute(name);
   }

   public SingularAttribute createSingularAttribute(String name) {
      SingularAttribute attribute = new SingularAttributeImpl(name, this);
      this.addAttribute(attribute);
      return attribute;
   }

   public SingularAttribute createVirtualSingularAttribute(String name) {
      throw new NotYetImplementedException();
   }

   public SingularAttribute locateComponentAttribute(String name) {
      return (SingularAttributeImpl)this.locateAttribute(name);
   }

   public SingularAttribute createComponentAttribute(String name, Component component) {
      SingularAttributeImpl attribute = new SingularAttributeImpl(name, this);
      attribute.resolveType(component);
      this.addAttribute(attribute);
      return attribute;
   }

   public PluralAttribute locatePluralAttribute(String name) {
      return (PluralAttribute)this.locateAttribute(name);
   }

   protected PluralAttribute createPluralAttribute(String name, PluralAttributeNature nature) {
      PluralAttribute attribute = (PluralAttribute)(nature.isIndexed() ? new IndexedPluralAttributeImpl(name, nature, this) : new PluralAttributeImpl(name, nature, this));
      this.addAttribute(attribute);
      return attribute;
   }

   public PluralAttribute locateBag(String name) {
      return this.locatePluralAttribute(name);
   }

   public PluralAttribute createBag(String name) {
      return this.createPluralAttribute(name, PluralAttributeNature.BAG);
   }

   public PluralAttribute locateSet(String name) {
      return this.locatePluralAttribute(name);
   }

   public PluralAttribute createSet(String name) {
      return this.createPluralAttribute(name, PluralAttributeNature.SET);
   }

   public IndexedPluralAttribute locateList(String name) {
      return (IndexedPluralAttribute)this.locatePluralAttribute(name);
   }

   public IndexedPluralAttribute createList(String name) {
      return (IndexedPluralAttribute)this.createPluralAttribute(name, PluralAttributeNature.LIST);
   }

   public IndexedPluralAttribute locateMap(String name) {
      return (IndexedPluralAttribute)this.locatePluralAttribute(name);
   }

   public IndexedPluralAttribute createMap(String name) {
      return (IndexedPluralAttribute)this.createPluralAttribute(name, PluralAttributeNature.MAP);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("AbstractAttributeContainer");
      sb.append("{name='").append(this.name).append('\'');
      sb.append(", superType=").append(this.superType);
      sb.append('}');
      return sb.toString();
   }

   protected void addAttribute(Attribute attribute) {
      if (this.attributeMap.put(attribute.getName(), attribute) != null) {
         throw new IllegalArgumentException("Attribute with name [" + attribute.getName() + "] already registered");
      } else {
         this.attributeSet.add(attribute);
      }
   }

   public static class SingularAttributeImpl implements SingularAttribute {
      private final AttributeContainer attributeContainer;
      private final String name;
      private Type type;

      public SingularAttributeImpl(String name, AttributeContainer attributeContainer) {
         this.name = name;
         this.attributeContainer = attributeContainer;
      }

      public boolean isTypeResolved() {
         return this.type != null;
      }

      public void resolveType(Type type) {
         if (type == null) {
            throw new IllegalArgumentException("Attempt to resolve with null type");
         } else {
            this.type = type;
         }
      }

      public Type getSingularAttributeType() {
         return this.type;
      }

      public String getName() {
         return this.name;
      }

      public AttributeContainer getAttributeContainer() {
         return this.attributeContainer;
      }

      public boolean isSingular() {
         return true;
      }
   }

   public static class PluralAttributeImpl implements PluralAttribute {
      private final AttributeContainer attributeContainer;
      private final PluralAttributeNature nature;
      private final String name;
      private Type elementType;

      public PluralAttributeImpl(String name, PluralAttributeNature nature, AttributeContainer attributeContainer) {
         this.name = name;
         this.nature = nature;
         this.attributeContainer = attributeContainer;
      }

      public AttributeContainer getAttributeContainer() {
         return this.attributeContainer;
      }

      public boolean isSingular() {
         return false;
      }

      public PluralAttributeNature getNature() {
         return this.nature;
      }

      public String getName() {
         return this.name;
      }

      public String getRole() {
         return StringHelper.qualify(this.attributeContainer.getRoleBaseName(), this.name);
      }

      public Type getElementType() {
         return this.elementType;
      }

      public void setElementType(Type elementType) {
         this.elementType = elementType;
      }
   }

   public static class IndexedPluralAttributeImpl extends PluralAttributeImpl implements IndexedPluralAttribute {
      private Type indexType;

      public IndexedPluralAttributeImpl(String name, PluralAttributeNature nature, AttributeContainer attributeContainer) {
         super(name, nature, attributeContainer);
      }

      public Type getIndexType() {
         return this.indexType;
      }

      public void setIndexType(Type indexType) {
         this.indexType = indexType;
      }
   }
}
