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

class TimestampAttributeSourceImpl implements SingularAttributeSource {
   private final JaxbHibernateMapping.JaxbClass.JaxbTimestamp timestampElement;
   private final LocalBindingContext bindingContext;
   private final List valueSources;
   private final ExplicitHibernateTypeSource typeSource = new ExplicitHibernateTypeSource() {
      public String getName() {
         return "db".equals(TimestampAttributeSourceImpl.this.timestampElement.getSource()) ? "dbtimestamp" : "timestamp";
      }

      public Map getParameters() {
         return null;
      }
   };
   private ValueHolder propertyGenerationValue = new ValueHolder(new ValueHolder.DeferredInitializer() {
      public PropertyGeneration initialize() {
         PropertyGeneration propertyGeneration = TimestampAttributeSourceImpl.this.timestampElement.getGenerated() == null ? PropertyGeneration.NEVER : PropertyGeneration.parse(TimestampAttributeSourceImpl.this.timestampElement.getGenerated().value());
         if (propertyGeneration == PropertyGeneration.INSERT) {
            throw new MappingException("'generated' attribute cannot be 'insert' for versioning property", TimestampAttributeSourceImpl.this.bindingContext.getOrigin());
         } else {
            return propertyGeneration;
         }
      }
   });

   TimestampAttributeSourceImpl(final JaxbHibernateMapping.JaxbClass.JaxbTimestamp timestampElement, LocalBindingContext bindingContext) {
      this.timestampElement = timestampElement;
      this.bindingContext = bindingContext;
      this.valueSources = Helper.buildValueSources(new Helper.ValueSourcesAdapter() {
         public String getColumnAttribute() {
            return timestampElement.getColumn();
         }

         public String getFormulaAttribute() {
            return null;
         }

         public List getColumnOrFormulaElements() {
            return null;
         }

         public String getContainingTableName() {
            return null;
         }

         public boolean isIncludedInInsertByDefault() {
            return true;
         }

         public boolean isIncludedInUpdateByDefault() {
            return true;
         }
      }, bindingContext);
   }

   public String getName() {
      return this.timestampElement.getName();
   }

   public ExplicitHibernateTypeSource getTypeInformation() {
      return this.typeSource;
   }

   public String getPropertyAccessorName() {
      return this.timestampElement.getAccess();
   }

   public boolean isInsertable() {
      return true;
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
      return Helper.buildMetaAttributeSources(this.timestampElement.getMeta());
   }
}
