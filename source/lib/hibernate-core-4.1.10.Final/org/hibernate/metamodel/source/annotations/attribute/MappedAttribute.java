package org.hibernate.metamodel.source.annotations.attribute;

import java.util.List;
import java.util.Map;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.attribute.type.AttributeTypeResolver;
import org.hibernate.metamodel.source.annotations.entity.EntityBindingContext;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

public abstract class MappedAttribute implements Comparable {
   private final Map annotations;
   private final String name;
   private final Class attributeType;
   private final String accessType;
   private ColumnValues columnValues;
   private final boolean isId;
   private final boolean isOptimisticLockable;
   private final EntityBindingContext context;

   MappedAttribute(String name, Class attributeType, String accessType, Map annotations, EntityBindingContext context) {
      this.context = context;
      this.annotations = annotations;
      this.name = name;
      this.attributeType = attributeType;
      this.accessType = accessType;
      AnnotationInstance idAnnotation = JandexHelper.getSingleAnnotation(annotations, JPADotNames.ID);
      AnnotationInstance embeddedIdAnnotation = JandexHelper.getSingleAnnotation(annotations, JPADotNames.EMBEDDED_ID);
      this.isId = idAnnotation != null || embeddedIdAnnotation != null;
      AnnotationInstance columnAnnotation = JandexHelper.getSingleAnnotation(annotations, JPADotNames.COLUMN);
      this.columnValues = new ColumnValues(columnAnnotation);
      this.isOptimisticLockable = this.checkOptimisticLockAnnotation();
   }

   public String getName() {
      return this.name;
   }

   public final Class getAttributeType() {
      return this.attributeType;
   }

   public String getAccessType() {
      return this.accessType;
   }

   public EntityBindingContext getContext() {
      return this.context;
   }

   public Map annotations() {
      return this.annotations;
   }

   public ColumnValues getColumnValues() {
      return this.columnValues;
   }

   public boolean isId() {
      return this.isId;
   }

   public boolean isOptimisticLockable() {
      return this.isOptimisticLockable;
   }

   public int compareTo(MappedAttribute mappedProperty) {
      return this.name.compareTo(mappedProperty.getName());
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("MappedAttribute");
      sb.append("{name='").append(this.name).append('\'');
      sb.append('}');
      return sb.toString();
   }

   public abstract AttributeTypeResolver getHibernateTypeResolver();

   public abstract boolean isLazy();

   public abstract boolean isOptional();

   public abstract boolean isInsertable();

   public abstract boolean isUpdatable();

   public abstract PropertyGeneration getPropertyGeneration();

   private boolean checkOptimisticLockAnnotation() {
      boolean triggersVersionIncrement = true;
      AnnotationInstance optimisticLockAnnotation = JandexHelper.getSingleAnnotation(this.annotations(), HibernateDotNames.OPTIMISTIC_LOCK);
      if (optimisticLockAnnotation != null) {
         boolean exclude = optimisticLockAnnotation.value("excluded").asBoolean();
         triggersVersionIncrement = !exclude;
      }

      return triggersVersionIncrement;
   }
}
