package org.hibernate.metamodel.source.hbm;

import java.util.List;
import java.util.Map;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbHibernateMapping;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.SingularAttributeNature;
import org.hibernate.metamodel.source.binder.SingularAttributeSource;

class VersionAttributeSourceImpl implements SingularAttributeSource {
   private final JaxbHibernateMapping.JaxbClass.JaxbVersion versionElement;
   private final LocalBindingContext bindingContext;
   private final List valueSources;
   private final ExplicitHibernateTypeSource typeSource = new ExplicitHibernateTypeSource() {
      public String getName() {
         return VersionAttributeSourceImpl.this.versionElement.getType() == null ? "integer" : VersionAttributeSourceImpl.this.versionElement.getType();
      }

      public Map getParameters() {
         return null;
      }
   };
   private ValueHolder propertyGenerationValue = new ValueHolder(new ValueHolder.DeferredInitializer() {
      public PropertyGeneration initialize() {
         PropertyGeneration propertyGeneration = VersionAttributeSourceImpl.this.versionElement.getGenerated() == null ? PropertyGeneration.NEVER : PropertyGeneration.parse(VersionAttributeSourceImpl.this.versionElement.getGenerated().value());
         if (propertyGeneration == PropertyGeneration.INSERT) {
            throw new MappingException("'generated' attribute cannot be 'insert' for versioning property", VersionAttributeSourceImpl.this.bindingContext.getOrigin());
         } else {
            return propertyGeneration;
         }
      }
   });

   VersionAttributeSourceImpl(final JaxbHibernateMapping.JaxbClass.JaxbVersion versionElement, LocalBindingContext bindingContext) {
      this.versionElement = versionElement;
      this.bindingContext = bindingContext;
      this.valueSources = Helper.buildValueSources(new Helper.ValueSourcesAdapter() {
         public String getColumnAttribute() {
            return versionElement.getColumnAttribute();
         }

         public String getFormulaAttribute() {
            return null;
         }

         public List getColumnOrFormulaElements() {
            return versionElement.getColumn();
         }

         public String getContainingTableName() {
            return null;
         }

         public boolean isIncludedInInsertByDefault() {
            return Helper.getBooleanValue(versionElement.isInsert(), true);
         }

         public boolean isIncludedInUpdateByDefault() {
            return true;
         }
      }, bindingContext);
   }

   public String getName() {
      return this.versionElement.getName();
   }

   public ExplicitHibernateTypeSource getTypeInformation() {
      return this.typeSource;
   }

   public String getPropertyAccessorName() {
      return this.versionElement.getAccess();
   }

   public boolean isInsertable() {
      return Helper.getBooleanValue(this.versionElement.isInsert(), true);
   }

   public boolean isUpdatable() {
      return true;
   }

   public PropertyGeneration getGeneration() {
      return (PropertyGeneration)this.propertyGenerationValue.getValue();
   }

   public boolean isLazy() {
      return false;
   }

   public boolean isIncludedInOptimisticLocking() {
      return false;
   }

   public SingularAttributeNature getNature() {
      return SingularAttributeNature.BASIC;
   }

   public boolean isVirtualAttribute() {
      return false;
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

   public List relationalValueSources() {
      return this.valueSources;
   }

   public boolean isSingular() {
      return true;
   }

   public Iterable metaAttributes() {
      return Helper.buildMetaAttributeSources(this.versionElement.getMeta());
   }
}
