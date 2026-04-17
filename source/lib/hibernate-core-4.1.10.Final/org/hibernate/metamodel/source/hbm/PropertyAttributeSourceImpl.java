package org.hibernate.metamodel.source.hbm;

import java.util.List;
import java.util.Map;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbPropertyElement;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.SingularAttributeNature;
import org.hibernate.metamodel.source.binder.SingularAttributeSource;

class PropertyAttributeSourceImpl implements SingularAttributeSource {
   private final JaxbPropertyElement propertyElement;
   private final ExplicitHibernateTypeSource typeSource;
   private final List valueSources;

   PropertyAttributeSourceImpl(final JaxbPropertyElement propertyElement, LocalBindingContext bindingContext) {
      this.propertyElement = propertyElement;
      this.typeSource = new ExplicitHibernateTypeSource() {
         private final String name = propertyElement.getTypeAttribute() != null ? propertyElement.getTypeAttribute() : (propertyElement.getType() != null ? propertyElement.getType().getName() : null);
         private final Map parameters = propertyElement.getType() != null ? Helper.extractParameters(propertyElement.getType().getParam()) : null;

         public String getName() {
            return this.name;
         }

         public Map getParameters() {
            return this.parameters;
         }
      };
      this.valueSources = Helper.buildValueSources(new Helper.ValueSourcesAdapter() {
         public String getColumnAttribute() {
            return propertyElement.getColumn();
         }

         public String getFormulaAttribute() {
            return propertyElement.getFormula();
         }

         public List getColumnOrFormulaElements() {
            return propertyElement.getColumnOrFormula();
         }

         public String getContainingTableName() {
            return null;
         }

         public boolean isIncludedInInsertByDefault() {
            return Helper.getBooleanValue(propertyElement.isInsert(), true);
         }

         public boolean isIncludedInUpdateByDefault() {
            return Helper.getBooleanValue(propertyElement.isUpdate(), true);
         }
      }, bindingContext);
   }

   public String getName() {
      return this.propertyElement.getName();
   }

   public ExplicitHibernateTypeSource getTypeInformation() {
      return this.typeSource;
   }

   public String getPropertyAccessorName() {
      return this.propertyElement.getAccess();
   }

   public boolean isInsertable() {
      return Helper.getBooleanValue(this.propertyElement.isInsert(), true);
   }

   public boolean isUpdatable() {
      return Helper.getBooleanValue(this.propertyElement.isUpdate(), true);
   }

   public PropertyGeneration getGeneration() {
      return PropertyGeneration.parse(this.propertyElement.getGenerated());
   }

   public boolean isLazy() {
      return Helper.getBooleanValue(this.propertyElement.isLazy(), false);
   }

   public boolean isIncludedInOptimisticLocking() {
      return Helper.getBooleanValue(this.propertyElement.isOptimisticLock(), true);
   }

   public SingularAttributeNature getNature() {
      return SingularAttributeNature.BASIC;
   }

   public boolean isVirtualAttribute() {
      return false;
   }

   public boolean areValuesIncludedInInsertByDefault() {
      return Helper.getBooleanValue(this.propertyElement.isInsert(), true);
   }

   public boolean areValuesIncludedInUpdateByDefault() {
      return Helper.getBooleanValue(this.propertyElement.isUpdate(), true);
   }

   public boolean areValuesNullableByDefault() {
      return !Helper.getBooleanValue(this.propertyElement.isNotNull(), false);
   }

   public List relationalValueSources() {
      return this.valueSources;
   }

   public boolean isSingular() {
      return true;
   }

   public Iterable metaAttributes() {
      return Helper.buildMetaAttributeSources(this.propertyElement.getMeta());
   }
}
