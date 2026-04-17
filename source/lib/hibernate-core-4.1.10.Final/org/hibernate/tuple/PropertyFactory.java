package org.hibernate.tuple;

import java.lang.reflect.Constructor;
import org.hibernate.EntityMode;
import org.hibernate.FetchMode;
import org.hibernate.engine.internal.UnsavedValueFactory;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.IdentifierValue;
import org.hibernate.engine.spi.VersionValue;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.binding.AbstractPluralAttributeBinding;
import org.hibernate.metamodel.binding.AssociationAttributeBinding;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.BasicAttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.SimpleValueBinding;
import org.hibernate.metamodel.binding.SingularAttributeBinding;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;
import org.hibernate.type.VersionType;

public class PropertyFactory {
   public static IdentifierProperty buildIdentifierProperty(PersistentClass mappedEntity, IdentifierGenerator generator) {
      String mappedUnsavedValue = mappedEntity.getIdentifier().getNullValue();
      Type type = mappedEntity.getIdentifier().getType();
      org.hibernate.mapping.Property property = mappedEntity.getIdentifierProperty();
      IdentifierValue unsavedValue = UnsavedValueFactory.getUnsavedIdentifierValue(mappedUnsavedValue, getGetter(property), type, getConstructor(mappedEntity));
      return property == null ? new IdentifierProperty(type, mappedEntity.hasEmbeddedIdentifier(), mappedEntity.hasIdentifierMapper(), unsavedValue, generator) : new IdentifierProperty(property.getName(), property.getNodeName(), type, mappedEntity.hasEmbeddedIdentifier(), unsavedValue, generator);
   }

   public static IdentifierProperty buildIdentifierProperty(EntityBinding mappedEntity, IdentifierGenerator generator) {
      BasicAttributeBinding property = mappedEntity.getHierarchyDetails().getEntityIdentifier().getValueBinding();
      String mappedUnsavedValue = property.getUnsavedValue();
      Type type = property.getHibernateTypeDescriptor().getResolvedTypeMapping();
      IdentifierValue unsavedValue = UnsavedValueFactory.getUnsavedIdentifierValue(mappedUnsavedValue, getGetter((AttributeBinding)property), type, getConstructor(mappedEntity));
      return property == null ? new IdentifierProperty(type, mappedEntity.getHierarchyDetails().getEntityIdentifier().isEmbedded(), mappedEntity.getHierarchyDetails().getEntityIdentifier().isIdentifierMapper(), unsavedValue, generator) : new IdentifierProperty(property.getAttribute().getName(), (String)null, type, mappedEntity.getHierarchyDetails().getEntityIdentifier().isEmbedded(), unsavedValue, generator);
   }

   public static VersionProperty buildVersionProperty(org.hibernate.mapping.Property property, boolean lazyAvailable) {
      String mappedUnsavedValue = ((KeyValue)property.getValue()).getNullValue();
      VersionValue unsavedValue = UnsavedValueFactory.getUnsavedVersionValue(mappedUnsavedValue, getGetter(property), (VersionType)property.getType(), getConstructor(property.getPersistentClass()));
      boolean lazy = lazyAvailable && property.isLazy();
      return new VersionProperty(property.getName(), property.getNodeName(), property.getValue().getType(), lazy, property.isInsertable(), property.isUpdateable(), property.getGeneration() == PropertyGeneration.INSERT || property.getGeneration() == PropertyGeneration.ALWAYS, property.getGeneration() == PropertyGeneration.ALWAYS, property.isOptional(), property.isUpdateable() && !lazy, property.isOptimisticLocked(), property.getCascadeStyle(), unsavedValue);
   }

   public static VersionProperty buildVersionProperty(BasicAttributeBinding property, boolean lazyAvailable) {
      String mappedUnsavedValue = ((KeyValue)property.getValue()).getNullValue();
      VersionValue unsavedValue = UnsavedValueFactory.getUnsavedVersionValue(mappedUnsavedValue, getGetter((AttributeBinding)property), (VersionType)property.getHibernateTypeDescriptor().getResolvedTypeMapping(), getConstructor((EntityBinding)property.getContainer()));
      boolean lazy = lazyAvailable && property.isLazy();
      CascadeStyle cascadeStyle = property.isAssociation() ? ((AssociationAttributeBinding)property).getCascadeStyle() : CascadeStyle.NONE;
      return new VersionProperty(property.getAttribute().getName(), (String)null, property.getHibernateTypeDescriptor().getResolvedTypeMapping(), lazy, true, true, property.getGeneration() == PropertyGeneration.INSERT || property.getGeneration() == PropertyGeneration.ALWAYS, property.getGeneration() == PropertyGeneration.ALWAYS, property.isNullable(), !lazy, property.isIncludedInOptimisticLocking(), cascadeStyle, unsavedValue);
   }

   public static StandardProperty buildStandardProperty(org.hibernate.mapping.Property property, boolean lazyAvailable) {
      Type type = property.getValue().getType();
      boolean alwaysDirtyCheck = type.isAssociationType() && ((AssociationType)type).isAlwaysDirtyChecked();
      return new StandardProperty(property.getName(), property.getNodeName(), type, lazyAvailable && property.isLazy(), property.isInsertable(), property.isUpdateable(), property.getGeneration() == PropertyGeneration.INSERT || property.getGeneration() == PropertyGeneration.ALWAYS, property.getGeneration() == PropertyGeneration.ALWAYS, property.isOptional(), alwaysDirtyCheck || property.isUpdateable(), property.isOptimisticLocked(), property.getCascadeStyle(), property.getValue().getFetchMode());
   }

   public static StandardProperty buildStandardProperty(AttributeBinding property, boolean lazyAvailable) {
      Type type = property.getHibernateTypeDescriptor().getResolvedTypeMapping();
      boolean alwaysDirtyCheck = type.isAssociationType() && ((AssociationType)type).isAlwaysDirtyChecked();
      if (property.getAttribute().isSingular()) {
         SingularAttributeBinding singularAttributeBinding = (SingularAttributeBinding)property;
         CascadeStyle cascadeStyle = singularAttributeBinding.isAssociation() ? ((AssociationAttributeBinding)singularAttributeBinding).getCascadeStyle() : CascadeStyle.NONE;
         FetchMode fetchMode = singularAttributeBinding.isAssociation() ? ((AssociationAttributeBinding)singularAttributeBinding).getFetchMode() : FetchMode.DEFAULT;
         return new StandardProperty(singularAttributeBinding.getAttribute().getName(), (String)null, type, lazyAvailable && singularAttributeBinding.isLazy(), true, true, singularAttributeBinding.getGeneration() == PropertyGeneration.INSERT || singularAttributeBinding.getGeneration() == PropertyGeneration.ALWAYS, singularAttributeBinding.getGeneration() == PropertyGeneration.ALWAYS, singularAttributeBinding.isNullable(), alwaysDirtyCheck || areAllValuesIncludedInUpdate(singularAttributeBinding), singularAttributeBinding.isIncludedInOptimisticLocking(), cascadeStyle, fetchMode);
      } else {
         AbstractPluralAttributeBinding pluralAttributeBinding = (AbstractPluralAttributeBinding)property;
         CascadeStyle cascadeStyle = pluralAttributeBinding.isAssociation() ? pluralAttributeBinding.getCascadeStyle() : CascadeStyle.NONE;
         FetchMode fetchMode = pluralAttributeBinding.isAssociation() ? pluralAttributeBinding.getFetchMode() : FetchMode.DEFAULT;
         return new StandardProperty(pluralAttributeBinding.getAttribute().getName(), (String)null, type, lazyAvailable && pluralAttributeBinding.isLazy(), true, true, false, false, false, true, pluralAttributeBinding.isIncludedInOptimisticLocking(), cascadeStyle, fetchMode);
      }
   }

   private static boolean areAllValuesIncludedInUpdate(SingularAttributeBinding attributeBinding) {
      if (attributeBinding.hasDerivedValue()) {
         return false;
      } else {
         for(SimpleValueBinding valueBinding : attributeBinding.getSimpleValueBindings()) {
            if (!valueBinding.isIncludeInUpdate()) {
               return false;
            }
         }

         return true;
      }
   }

   private static Constructor getConstructor(PersistentClass persistentClass) {
      if (persistentClass != null && persistentClass.hasPojoRepresentation()) {
         try {
            return ReflectHelper.getDefaultConstructor(persistentClass.getMappedClass());
         } catch (Throwable var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Constructor getConstructor(EntityBinding entityBinding) {
      if (entityBinding != null && entityBinding.getEntity() != null) {
         try {
            return ReflectHelper.getDefaultConstructor(entityBinding.getEntity().getClassReference());
         } catch (Throwable var2) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static Getter getGetter(org.hibernate.mapping.Property mappingProperty) {
      if (mappingProperty != null && mappingProperty.getPersistentClass().hasPojoRepresentation()) {
         PropertyAccessor pa = PropertyAccessorFactory.getPropertyAccessor(mappingProperty, EntityMode.POJO);
         return pa.getGetter(mappingProperty.getPersistentClass().getMappedClass(), mappingProperty.getName());
      } else {
         return null;
      }
   }

   private static Getter getGetter(AttributeBinding mappingProperty) {
      if (mappingProperty != null && mappingProperty.getContainer().getClassReference() != null) {
         PropertyAccessor pa = PropertyAccessorFactory.getPropertyAccessor(mappingProperty, EntityMode.POJO);
         return pa.getGetter(mappingProperty.getContainer().getClassReference(), mappingProperty.getAttribute().getName());
      } else {
         return null;
      }
   }
}
