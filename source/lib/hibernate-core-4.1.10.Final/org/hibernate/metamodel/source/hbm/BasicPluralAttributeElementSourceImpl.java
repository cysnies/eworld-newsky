package org.hibernate.metamodel.source.hbm;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbElementElement;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.binder.BasicPluralAttributeElementSource;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.PluralAttributeElementNature;

public class BasicPluralAttributeElementSourceImpl implements BasicPluralAttributeElementSource {
   private final List valueSources;
   private final ExplicitHibernateTypeSource typeSource;

   public BasicPluralAttributeElementSourceImpl(final JaxbElementElement elementElement, LocalBindingContext bindingContext) {
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
            return elementElement.getColumn();
         }

         public String getFormulaAttribute() {
            return elementElement.getFormula();
         }

         public List getColumnOrFormulaElements() {
            return elementElement.getColumnOrFormula();
         }
      }, bindingContext);
      this.typeSource = new ExplicitHibernateTypeSource() {
         public String getName() {
            if (elementElement.getTypeAttribute() != null) {
               return elementElement.getTypeAttribute();
            } else {
               return elementElement.getType() != null ? elementElement.getType().getName() : null;
            }
         }

         public Map getParameters() {
            return elementElement.getType() != null ? Helper.extractParameters(elementElement.getType().getParam()) : Collections.emptyMap();
         }
      };
   }

   public PluralAttributeElementNature getNature() {
      return PluralAttributeElementNature.BASIC;
   }

   public List getValueSources() {
      return this.valueSources;
   }

   public ExplicitHibernateTypeSource getExplicitHibernateTypeSource() {
      return this.typeSource;
   }
}
