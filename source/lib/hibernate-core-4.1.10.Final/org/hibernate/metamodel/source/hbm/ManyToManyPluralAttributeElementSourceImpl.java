package org.hibernate.metamodel.source.hbm;

import java.util.List;
import org.hibernate.FetchMode;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbManyToManyElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.binder.ManyToManyPluralAttributeElementSource;
import org.hibernate.metamodel.source.binder.PluralAttributeElementNature;

public class ManyToManyPluralAttributeElementSourceImpl implements ManyToManyPluralAttributeElementSource {
   private final JaxbManyToManyElement manyToManyElement;
   private final LocalBindingContext bindingContext;
   private final List valueSources;

   public ManyToManyPluralAttributeElementSourceImpl(final JaxbManyToManyElement manyToManyElement, LocalBindingContext bindingContext) {
      this.manyToManyElement = manyToManyElement;
      this.bindingContext = bindingContext;
      this.valueSources = Helper.buildValueSources(new Helper.ValueSourcesAdapter() {
         public String getContainingTableName() {
            return null;
         }

         public boolean isIncludedInInsertByDefault() {
            return true;
         }

         public boolean isIncludedInUpdateByDefault() {
            return true;
         }

         public String getColumnAttribute() {
            return manyToManyElement.getColumn();
         }

         public String getFormulaAttribute() {
            return manyToManyElement.getFormula();
         }

         public List getColumnOrFormulaElements() {
            return manyToManyElement.getColumnOrFormula();
         }
      }, bindingContext);
   }

   public PluralAttributeElementNature getNature() {
      return PluralAttributeElementNature.MANY_TO_MANY;
   }

   public String getReferencedEntityName() {
      return StringHelper.isNotEmpty(this.manyToManyElement.getEntityName()) ? this.manyToManyElement.getEntityName() : this.bindingContext.qualifyClassName(this.manyToManyElement.getClazz());
   }

   public String getReferencedEntityAttributeName() {
      return this.manyToManyElement.getPropertyRef();
   }

   public List getValueSources() {
      return this.valueSources;
   }

   public boolean isNotFoundAnException() {
      return this.manyToManyElement.getNotFound() == null || !"ignore".equals(this.manyToManyElement.getNotFound().value());
   }

   public String getExplicitForeignKeyName() {
      return this.manyToManyElement.getForeignKey();
   }

   public boolean isUnique() {
      return this.manyToManyElement.isUnique();
   }

   public String getOrderBy() {
      return this.manyToManyElement.getOrderBy();
   }

   public String getWhere() {
      return this.manyToManyElement.getWhere();
   }

   public FetchMode getFetchMode() {
      return null;
   }

   public boolean fetchImmediately() {
      if (this.manyToManyElement.getLazy() != null && "false".equals(this.manyToManyElement.getLazy().value())) {
         return true;
      } else if (this.manyToManyElement.getOuterJoin() == null) {
         return !this.bindingContext.getMappingDefaults().areAssociationsLazy();
      } else {
         String value = this.manyToManyElement.getOuterJoin().value();
         if ("auto".equals(value)) {
            return !this.bindingContext.getMappingDefaults().areAssociationsLazy();
         } else {
            return "true".equals(value);
         }
      }
   }
}
