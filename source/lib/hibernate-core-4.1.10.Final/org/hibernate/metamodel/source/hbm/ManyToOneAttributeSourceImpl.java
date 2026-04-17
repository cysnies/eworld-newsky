package org.hibernate.metamodel.source.hbm;

import java.util.List;
import org.hibernate.FetchMode;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbManyToOneElement;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.SingularAttributeNature;
import org.hibernate.metamodel.source.binder.ToOneAttributeSource;

class ManyToOneAttributeSourceImpl implements ToOneAttributeSource {
   private final JaxbManyToOneElement manyToOneElement;
   private final LocalBindingContext bindingContext;
   private final List valueSources;

   ManyToOneAttributeSourceImpl(final JaxbManyToOneElement manyToOneElement, LocalBindingContext bindingContext) {
      this.manyToOneElement = manyToOneElement;
      this.bindingContext = bindingContext;
      this.valueSources = Helper.buildValueSources(new Helper.ValueSourcesAdapter() {
         public String getColumnAttribute() {
            return manyToOneElement.getColumn();
         }

         public String getFormulaAttribute() {
            return manyToOneElement.getFormula();
         }

         public List getColumnOrFormulaElements() {
            return manyToOneElement.getColumnOrFormula();
         }

         public String getContainingTableName() {
            return null;
         }

         public boolean isIncludedInInsertByDefault() {
            return manyToOneElement.isInsert();
         }

         public boolean isIncludedInUpdateByDefault() {
            return manyToOneElement.isUpdate();
         }
      }, bindingContext);
   }

   public String getName() {
      return this.manyToOneElement.getName();
   }

   public ExplicitHibernateTypeSource getTypeInformation() {
      return Helper.TO_ONE_ATTRIBUTE_TYPE_SOURCE;
   }

   public String getPropertyAccessorName() {
      return this.manyToOneElement.getAccess();
   }

   public boolean isInsertable() {
      return this.manyToOneElement.isInsert();
   }

   public boolean isUpdatable() {
      return this.manyToOneElement.isUpdate();
   }

   public PropertyGeneration getGeneration() {
      return PropertyGeneration.NEVER;
   }

   public boolean isLazy() {
      return false;
   }

   public boolean isIncludedInOptimisticLocking() {
      return this.manyToOneElement.isOptimisticLock();
   }

   public Iterable getCascadeStyles() {
      return Helper.interpretCascadeStyles(this.manyToOneElement.getCascade(), this.bindingContext);
   }

   public FetchTiming getFetchTiming() {
      String fetchSelection = this.manyToOneElement.getFetch() != null ? this.manyToOneElement.getFetch().value() : null;
      String lazySelection = this.manyToOneElement.getLazy() != null ? this.manyToOneElement.getLazy().value() : null;
      String outerJoinSelection = this.manyToOneElement.getOuterJoin() != null ? this.manyToOneElement.getOuterJoin().value() : null;
      if (lazySelection == null) {
         if (!"join".equals(fetchSelection) && !"true".equals(outerJoinSelection)) {
            if ("false".equals(outerJoinSelection)) {
               return FetchTiming.DELAYED;
            } else {
               return this.bindingContext.getMappingDefaults().areAssociationsLazy() ? FetchTiming.DELAYED : FetchTiming.IMMEDIATE;
            }
         } else {
            return FetchTiming.IMMEDIATE;
         }
      } else if ("extra".equals(lazySelection)) {
         return FetchTiming.EXTRA_LAZY;
      } else if ("true".equals(lazySelection)) {
         return FetchTiming.DELAYED;
      } else if ("false".equals(lazySelection)) {
         return FetchTiming.IMMEDIATE;
      } else {
         throw new MappingException(String.format("Unexpected lazy selection [%s] on '%s'", lazySelection, this.manyToOneElement.getName()), this.bindingContext.getOrigin());
      }
   }

   public FetchStyle getFetchStyle() {
      String fetchSelection = this.manyToOneElement.getFetch() != null ? this.manyToOneElement.getFetch().value() : null;
      String outerJoinSelection = this.manyToOneElement.getOuterJoin() != null ? this.manyToOneElement.getOuterJoin().value() : null;
      if (fetchSelection == null) {
         if (outerJoinSelection == null) {
            return FetchStyle.SELECT;
         } else if ("auto".equals(outerJoinSelection)) {
            return this.bindingContext.getMappingDefaults().areAssociationsLazy() ? FetchStyle.SELECT : FetchStyle.JOIN;
         } else {
            return "true".equals(outerJoinSelection) ? FetchStyle.JOIN : FetchStyle.SELECT;
         }
      } else {
         return "join".equals(fetchSelection) ? FetchStyle.JOIN : FetchStyle.SELECT;
      }
   }

   public FetchMode getFetchMode() {
      return this.manyToOneElement.getFetch() == null ? FetchMode.DEFAULT : FetchMode.valueOf(this.manyToOneElement.getFetch().value());
   }

   public SingularAttributeNature getNature() {
      return SingularAttributeNature.MANY_TO_ONE;
   }

   public boolean isVirtualAttribute() {
      return false;
   }

   public boolean areValuesIncludedInInsertByDefault() {
      return this.manyToOneElement.isInsert();
   }

   public boolean areValuesIncludedInUpdateByDefault() {
      return this.manyToOneElement.isUpdate();
   }

   public boolean areValuesNullableByDefault() {
      return !Helper.getBooleanValue(this.manyToOneElement.isNotNull(), false);
   }

   public List relationalValueSources() {
      return this.valueSources;
   }

   public boolean isSingular() {
      return true;
   }

   public Iterable metaAttributes() {
      return Helper.buildMetaAttributeSources(this.manyToOneElement.getMeta());
   }

   public String getReferencedEntityName() {
      return this.manyToOneElement.getClazz() != null ? this.manyToOneElement.getClazz() : this.manyToOneElement.getEntityName();
   }

   public String getReferencedEntityAttributeName() {
      return this.manyToOneElement.getPropertyRef();
   }
}
