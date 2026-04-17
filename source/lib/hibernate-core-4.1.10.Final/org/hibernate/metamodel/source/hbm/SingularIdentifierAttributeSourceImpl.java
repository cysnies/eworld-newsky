package org.hibernate.metamodel.source.hbm;

import java.util.List;
import java.util.Map;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbHibernateMapping;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.SingularAttributeNature;
import org.hibernate.metamodel.source.binder.SingularAttributeSource;

class SingularIdentifierAttributeSourceImpl implements SingularAttributeSource {
   private final JaxbHibernateMapping.JaxbClass.JaxbId idElement;
   private final ExplicitHibernateTypeSource typeSource;
   private final List valueSources;

   public SingularIdentifierAttributeSourceImpl(final JaxbHibernateMapping.JaxbClass.JaxbId idElement, LocalBindingContext bindingContext) {
      this.idElement = idElement;
      this.typeSource = new ExplicitHibernateTypeSource() {
         private final String name = idElement.getTypeAttribute() != null ? idElement.getTypeAttribute() : (idElement.getType() != null ? idElement.getType().getName() : null);
         private final Map parameters = idElement.getType() != null ? Helper.extractParameters(idElement.getType().getParam()) : null;

         public String getName() {
            return this.name;
         }

         public Map getParameters() {
            return this.parameters;
         }
      };
      this.valueSources = Helper.buildValueSources(new Helper.ValueSourcesAdapter() {
         public String getColumnAttribute() {
            return idElement.getColumnAttribute();
         }

         public String getFormulaAttribute() {
            return null;
         }

         public List getColumnOrFormulaElements() {
            return idElement.getColumn();
         }

         public String getContainingTableName() {
            return null;
         }

         public boolean isIncludedInInsertByDefault() {
            return true;
         }

         public boolean isIncludedInUpdateByDefault() {
            return false;
         }

         public boolean isForceNotNull() {
            return true;
         }
      }, bindingContext);
   }

   public String getName() {
      return this.idElement.getName() == null ? "id" : this.idElement.getName();
   }

   public ExplicitHibernateTypeSource getTypeInformation() {
      return this.typeSource;
   }

   public String getPropertyAccessorName() {
      return this.idElement.getAccess();
   }

   public boolean isInsertable() {
      return true;
   }

   public boolean isUpdatable() {
      return false;
   }

   public PropertyGeneration getGeneration() {
      return PropertyGeneration.INSERT;
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
      return false;
   }

   public List relationalValueSources() {
      return this.valueSources;
   }

   public boolean isSingular() {
      return true;
   }

   public Iterable metaAttributes() {
      return Helper.buildMetaAttributeSources(this.idElement.getMeta());
   }
}
