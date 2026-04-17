package org.hibernate.metamodel.source.hbm;

import java.util.List;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbKeyElement;
import org.hibernate.metamodel.relational.ForeignKey;
import org.hibernate.metamodel.source.binder.AttributeSourceContainer;
import org.hibernate.metamodel.source.binder.PluralAttributeKeySource;

public class PluralAttributeKeySourceImpl implements PluralAttributeKeySource {
   private final JaxbKeyElement keyElement;
   private final List valueSources;

   public PluralAttributeKeySourceImpl(final JaxbKeyElement keyElement, AttributeSourceContainer container) {
      this.keyElement = keyElement;
      this.valueSources = Helper.buildValueSources(new Helper.ValueSourcesAdapter() {
         public String getContainingTableName() {
            return null;
         }

         public boolean isIncludedInInsertByDefault() {
            return true;
         }

         public boolean isIncludedInUpdateByDefault() {
            return Helper.getBooleanValue(keyElement.isUpdate(), true);
         }

         public String getColumnAttribute() {
            return keyElement.getColumnAttribute();
         }

         public String getFormulaAttribute() {
            return null;
         }

         public List getColumnOrFormulaElements() {
            return keyElement.getColumn();
         }
      }, container.getLocalBindingContext());
   }

   public List getValueSources() {
      return this.valueSources;
   }

   public String getExplicitForeignKeyName() {
      return this.keyElement.getForeignKey();
   }

   public ForeignKey.ReferentialAction getOnDeleteAction() {
      return "cascade".equals(this.keyElement.getOnDelete()) ? ForeignKey.ReferentialAction.CASCADE : ForeignKey.ReferentialAction.NO_ACTION;
   }

   public String getReferencedEntityAttributeName() {
      return this.keyElement.getPropertyRef();
   }
}
