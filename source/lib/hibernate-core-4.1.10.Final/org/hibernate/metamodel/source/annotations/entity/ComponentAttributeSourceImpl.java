package org.hibernate.metamodel.source.annotations.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.annotations.attribute.AssociationAttribute;
import org.hibernate.metamodel.source.annotations.attribute.AttributeOverride;
import org.hibernate.metamodel.source.annotations.attribute.BasicAttribute;
import org.hibernate.metamodel.source.annotations.attribute.SingularAttributeSourceImpl;
import org.hibernate.metamodel.source.annotations.attribute.ToOneAttributeSourceImpl;
import org.hibernate.metamodel.source.binder.AttributeSource;
import org.hibernate.metamodel.source.binder.ComponentAttributeSource;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.SingularAttributeNature;

public class ComponentAttributeSourceImpl implements ComponentAttributeSource {
   private static final String PATH_SEPERATOR = ".";
   private final EmbeddableClass embeddableClass;
   private final ValueHolder classReference;
   private final Map attributeOverrides;
   private final String path;

   public ComponentAttributeSourceImpl(EmbeddableClass embeddableClass, String parentPath, Map attributeOverrides) {
      this.embeddableClass = embeddableClass;
      this.classReference = new ValueHolder(embeddableClass.getConfiguredClass());
      this.attributeOverrides = attributeOverrides;
      if (StringHelper.isEmpty(parentPath)) {
         this.path = embeddableClass.getEmbeddedAttributeName();
      } else {
         this.path = parentPath + "." + embeddableClass.getEmbeddedAttributeName();
      }

   }

   public boolean isVirtualAttribute() {
      return false;
   }

   public SingularAttributeNature getNature() {
      return SingularAttributeNature.COMPONENT;
   }

   public boolean isSingular() {
      return true;
   }

   public String getClassName() {
      return this.embeddableClass.getConfiguredClass().getName();
   }

   public ValueHolder getClassReference() {
      return this.classReference;
   }

   public String getName() {
      return this.embeddableClass.getEmbeddedAttributeName();
   }

   public String getExplicitTuplizerClassName() {
      return this.embeddableClass.getCustomTuplizer();
   }

   public String getPropertyAccessorName() {
      return this.embeddableClass.getClassAccessType().toString().toLowerCase();
   }

   public LocalBindingContext getLocalBindingContext() {
      return this.embeddableClass.getLocalBindingContext();
   }

   public Iterable attributeSources() {
      List<AttributeSource> attributeList = new ArrayList();

      for(BasicAttribute attribute : this.embeddableClass.getSimpleAttributes()) {
         AttributeOverride attributeOverride = null;
         String tmp = this.getPath() + "." + attribute.getName();
         if (this.attributeOverrides.containsKey(tmp)) {
            attributeOverride = (AttributeOverride)this.attributeOverrides.get(tmp);
         }

         attributeList.add(new SingularAttributeSourceImpl(attribute, attributeOverride));
      }

      for(EmbeddableClass embeddable : this.embeddableClass.getEmbeddedClasses().values()) {
         attributeList.add(new ComponentAttributeSourceImpl(embeddable, this.getPath(), this.createAggregatedOverrideMap()));
      }

      for(AssociationAttribute associationAttribute : this.embeddableClass.getAssociationAttributes()) {
         attributeList.add(new ToOneAttributeSourceImpl(associationAttribute));
      }

      return attributeList;
   }

   public String getPath() {
      return this.path;
   }

   public String getParentReferenceAttributeName() {
      return this.embeddableClass.getParentReferencingAttributeName();
   }

   public Iterable metaAttributes() {
      return Collections.emptySet();
   }

   public List relationalValueSources() {
      return null;
   }

   public ExplicitHibernateTypeSource getTypeInformation() {
      return null;
   }

   public boolean isInsertable() {
      return true;
   }

   public boolean isUpdatable() {
      return true;
   }

   public PropertyGeneration getGeneration() {
      return null;
   }

   public boolean isLazy() {
      return false;
   }

   public boolean isIncludedInOptimisticLocking() {
      return true;
   }

   public boolean areValuesIncludedInInsertByDefault() {
      return true;
   }

   public boolean areValuesIncludedInUpdateByDefault() {
      return true;
   }

   public boolean areValuesNullableByDefault() {
      return true;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("ComponentAttributeSourceImpl");
      sb.append("{embeddableClass=").append(this.embeddableClass.getConfiguredClass().getSimpleName());
      sb.append('}');
      return sb.toString();
   }

   private Map createAggregatedOverrideMap() {
      Map<String, AttributeOverride> aggregatedOverrideMap = new HashMap(this.attributeOverrides);

      for(Map.Entry entry : this.embeddableClass.getAttributeOverrideMap().entrySet()) {
         String fullPath = this.getPath() + "." + (String)entry.getKey();
         if (!aggregatedOverrideMap.containsKey(fullPath)) {
            aggregatedOverrideMap.put(fullPath, entry.getValue());
         }
      }

      return aggregatedOverrideMap;
   }
}
