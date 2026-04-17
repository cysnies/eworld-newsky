package org.hibernate.metamodel.source.internal;

import java.util.Properties;
import org.hibernate.AssertionFailure;
import org.hibernate.metamodel.binding.AbstractCollectionElement;
import org.hibernate.metamodel.binding.AbstractPluralAttributeBinding;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.BasicCollectionElement;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.EntityDiscriminator;
import org.hibernate.metamodel.binding.HibernateTypeDescriptor;
import org.hibernate.metamodel.binding.SingularAttributeBinding;
import org.hibernate.metamodel.domain.SingularAttribute;
import org.hibernate.metamodel.relational.Datatype;
import org.hibernate.metamodel.relational.SimpleValue;
import org.hibernate.metamodel.relational.Value;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;

class HibernateTypeResolver {
   private final MetadataImplementor metadata;

   HibernateTypeResolver(MetadataImplementor metadata) {
      this.metadata = metadata;
   }

   void resolve() {
      for(EntityBinding entityBinding : this.metadata.getEntityBindings()) {
         if (entityBinding.getHierarchyDetails().getEntityDiscriminator() != null) {
            this.resolveDiscriminatorTypeInformation(entityBinding.getHierarchyDetails().getEntityDiscriminator());
         }

         for(AttributeBinding attributeBinding : entityBinding.attributeBindings()) {
            if (SingularAttributeBinding.class.isInstance(attributeBinding)) {
               this.resolveSingularAttributeTypeInformation((SingularAttributeBinding)SingularAttributeBinding.class.cast(attributeBinding));
            } else {
               if (!AbstractPluralAttributeBinding.class.isInstance(attributeBinding)) {
                  throw new AssertionFailure("Unknown type of AttributeBinding: " + attributeBinding.getClass().getName());
               }

               this.resolvePluralAttributeTypeInformation((AbstractPluralAttributeBinding)AbstractPluralAttributeBinding.class.cast(attributeBinding));
            }
         }
      }

   }

   private void resolveDiscriminatorTypeInformation(EntityDiscriminator discriminator) {
      Type resolvedHibernateType = this.determineSingularTypeFromDescriptor(discriminator.getExplicitHibernateTypeDescriptor());
      if (resolvedHibernateType != null) {
         this.pushHibernateTypeInformationDownIfNeeded(discriminator.getExplicitHibernateTypeDescriptor(), discriminator.getBoundValue(), resolvedHibernateType);
      }

   }

   private Type determineSingularTypeFromDescriptor(HibernateTypeDescriptor hibernateTypeDescriptor) {
      if (hibernateTypeDescriptor.getResolvedTypeMapping() != null) {
         return hibernateTypeDescriptor.getResolvedTypeMapping();
      } else {
         String typeName = determineTypeName(hibernateTypeDescriptor);
         Properties typeParameters = getTypeParameters(hibernateTypeDescriptor);
         return this.getHeuristicType(typeName, typeParameters);
      }
   }

   private static String determineTypeName(HibernateTypeDescriptor hibernateTypeDescriptor) {
      return hibernateTypeDescriptor.getExplicitTypeName() != null ? hibernateTypeDescriptor.getExplicitTypeName() : hibernateTypeDescriptor.getJavaTypeName();
   }

   private static Properties getTypeParameters(HibernateTypeDescriptor hibernateTypeDescriptor) {
      Properties typeParameters = new Properties();
      if (hibernateTypeDescriptor.getTypeParameters() != null) {
         typeParameters.putAll(hibernateTypeDescriptor.getTypeParameters());
      }

      return typeParameters;
   }

   private void resolveSingularAttributeTypeInformation(SingularAttributeBinding attributeBinding) {
      if (attributeBinding.getHibernateTypeDescriptor().getResolvedTypeMapping() == null) {
         Type resolvedType = this.determineSingularTypeFromDescriptor(attributeBinding.getHibernateTypeDescriptor());
         if (resolvedType == null) {
            if (!attributeBinding.getAttribute().isSingular()) {
               throw new AssertionFailure("SingularAttributeBinding object has a plural attribute: " + attributeBinding.getAttribute().getName());
            }

            SingularAttribute singularAttribute = (SingularAttribute)attributeBinding.getAttribute();
            if (singularAttribute.getSingularAttributeType() != null) {
               resolvedType = this.getHeuristicType(singularAttribute.getSingularAttributeType().getClassName(), new Properties());
            }
         }

         if (resolvedType != null) {
            this.pushHibernateTypeInformationDownIfNeeded(attributeBinding, resolvedType);
         }

      }
   }

   private void resolvePluralAttributeTypeInformation(AbstractPluralAttributeBinding attributeBinding) {
      if (attributeBinding.getHibernateTypeDescriptor().getResolvedTypeMapping() == null) {
         String typeName = attributeBinding.getHibernateTypeDescriptor().getExplicitTypeName();
         Type resolvedType;
         if (typeName != null) {
            resolvedType = this.metadata.getTypeResolver().getTypeFactory().customCollection(typeName, getTypeParameters(attributeBinding.getHibernateTypeDescriptor()), attributeBinding.getAttribute().getName(), attributeBinding.getReferencedPropertyName());
         } else {
            resolvedType = this.determineDefaultCollectionInformation(attributeBinding);
         }

         if (resolvedType != null) {
            this.pushHibernateTypeInformationDownIfNeeded(attributeBinding.getHibernateTypeDescriptor(), (Value)null, resolvedType);
         }

         this.resolveCollectionElementTypeInformation(attributeBinding.getCollectionElement());
      }
   }

   private Type determineDefaultCollectionInformation(AbstractPluralAttributeBinding attributeBinding) {
      TypeFactory typeFactory = this.metadata.getTypeResolver().getTypeFactory();
      switch (attributeBinding.getAttribute().getNature()) {
         case SET:
            return typeFactory.set(attributeBinding.getAttribute().getName(), attributeBinding.getReferencedPropertyName());
         case BAG:
            return typeFactory.bag(attributeBinding.getAttribute().getName(), attributeBinding.getReferencedPropertyName());
         default:
            throw new UnsupportedOperationException("Collection type not supported yet:" + attributeBinding.getAttribute().getNature());
      }
   }

   private void resolveCollectionElementTypeInformation(AbstractCollectionElement collectionElement) {
      switch (collectionElement.getCollectionElementNature()) {
         case BASIC:
            this.resolveBasicCollectionElement((BasicCollectionElement)BasicCollectionElement.class.cast(collectionElement));
            return;
         case COMPOSITE:
         case ONE_TO_MANY:
         case MANY_TO_MANY:
         case MANY_TO_ANY:
            throw new UnsupportedOperationException("Collection element nature not supported yet: " + collectionElement.getCollectionElementNature());
         default:
            throw new AssertionFailure("Unknown collection element nature : " + collectionElement.getCollectionElementNature());
      }
   }

   private void resolveBasicCollectionElement(BasicCollectionElement basicCollectionElement) {
      Type resolvedHibernateType = this.determineSingularTypeFromDescriptor(basicCollectionElement.getHibernateTypeDescriptor());
      if (resolvedHibernateType != null) {
         this.pushHibernateTypeInformationDownIfNeeded(basicCollectionElement.getHibernateTypeDescriptor(), basicCollectionElement.getElementValue(), resolvedHibernateType);
      }

   }

   private Type getHeuristicType(String typeName, Properties typeParameters) {
      if (typeName != null) {
         try {
            return this.metadata.getTypeResolver().heuristicType(typeName, typeParameters);
         } catch (Exception var4) {
         }
      }

      return null;
   }

   private void pushHibernateTypeInformationDownIfNeeded(SingularAttributeBinding attributeBinding, Type resolvedHibernateType) {
      HibernateTypeDescriptor hibernateTypeDescriptor = attributeBinding.getHibernateTypeDescriptor();
      SingularAttribute singularAttribute = (SingularAttribute)SingularAttribute.class.cast(attributeBinding.getAttribute());
      Value value = attributeBinding.getValue();
      if (!singularAttribute.isTypeResolved() && hibernateTypeDescriptor.getJavaTypeName() != null) {
         singularAttribute.resolveType(this.metadata.makeJavaType(hibernateTypeDescriptor.getJavaTypeName()));
      }

      this.pushHibernateTypeInformationDownIfNeeded(hibernateTypeDescriptor, value, resolvedHibernateType);
   }

   private void pushHibernateTypeInformationDownIfNeeded(HibernateTypeDescriptor hibernateTypeDescriptor, Value value, Type resolvedHibernateType) {
      if (resolvedHibernateType != null) {
         if (hibernateTypeDescriptor.getResolvedTypeMapping() == null) {
            hibernateTypeDescriptor.setResolvedTypeMapping(resolvedHibernateType);
         }

         if (hibernateTypeDescriptor.getJavaTypeName() == null) {
            hibernateTypeDescriptor.setJavaTypeName(resolvedHibernateType.getReturnedClass().getName());
         }

         if (SimpleValue.class.isInstance(value)) {
            SimpleValue simpleValue = (SimpleValue)value;
            if (simpleValue.getDatatype() == null) {
               simpleValue.setDatatype(new Datatype(resolvedHibernateType.sqlTypes(this.metadata)[0], resolvedHibernateType.getName(), resolvedHibernateType.getReturnedClass()));
            }
         }

      }
   }
}
