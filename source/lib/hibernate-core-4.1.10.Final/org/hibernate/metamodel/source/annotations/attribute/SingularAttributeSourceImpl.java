package org.hibernate.metamodel.source.annotations.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.RelationalValueSource;
import org.hibernate.metamodel.source.binder.SingularAttributeNature;
import org.hibernate.metamodel.source.binder.SingularAttributeSource;

public class SingularAttributeSourceImpl implements SingularAttributeSource {
   private final MappedAttribute attribute;
   private final AttributeOverride attributeOverride;

   public SingularAttributeSourceImpl(MappedAttribute attribute) {
      this(attribute, (AttributeOverride)null);
   }

   public SingularAttributeSourceImpl(MappedAttribute attribute, AttributeOverride attributeOverride) {
      this.attribute = attribute;
      this.attributeOverride = attributeOverride;
   }

   public ExplicitHibernateTypeSource getTypeInformation() {
      return new ExplicitHibernateTypeSourceImpl(this.attribute.getHibernateTypeResolver());
   }

   public String getPropertyAccessorName() {
      return this.attribute.getAccessType();
   }

   public boolean isInsertable() {
      return this.attribute.isInsertable();
   }

   public boolean isUpdatable() {
      return this.attribute.isUpdatable();
   }

   public PropertyGeneration getGeneration() {
      return this.attribute.getPropertyGeneration();
   }

   public boolean isLazy() {
      return this.attribute.isLazy();
   }

   public boolean isIncludedInOptimisticLocking() {
      return this.attribute.isOptimisticLockable();
   }

   public String getName() {
      return this.attribute.getName();
   }

   public List relationalValueSources() {
      List<RelationalValueSource> valueSources = new ArrayList();
      valueSources.add(new ColumnSourceImpl(this.attribute, this.attributeOverride));
      return valueSources;
   }

   public boolean isVirtualAttribute() {
      return false;
   }

   public boolean isSingular() {
      return true;
   }

   public SingularAttributeNature getNature() {
      return SingularAttributeNature.BASIC;
   }

   public Iterable metaAttributes() {
      return Collections.emptySet();
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
}
