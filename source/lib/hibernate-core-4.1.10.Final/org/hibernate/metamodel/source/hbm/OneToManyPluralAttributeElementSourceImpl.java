package org.hibernate.metamodel.source.hbm;

import org.hibernate.internal.jaxb.mapping.hbm.JaxbOneToManyElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.binder.OneToManyPluralAttributeElementSource;
import org.hibernate.metamodel.source.binder.PluralAttributeElementNature;

public class OneToManyPluralAttributeElementSourceImpl implements OneToManyPluralAttributeElementSource {
   private final JaxbOneToManyElement oneToManyElement;
   private final LocalBindingContext bindingContext;

   public OneToManyPluralAttributeElementSourceImpl(JaxbOneToManyElement oneToManyElement, LocalBindingContext bindingContext) {
      this.oneToManyElement = oneToManyElement;
      this.bindingContext = bindingContext;
   }

   public PluralAttributeElementNature getNature() {
      return PluralAttributeElementNature.ONE_TO_MANY;
   }

   public String getReferencedEntityName() {
      return StringHelper.isNotEmpty(this.oneToManyElement.getEntityName()) ? this.oneToManyElement.getEntityName() : this.bindingContext.qualifyClassName(this.oneToManyElement.getClazz());
   }

   public boolean isNotFoundAnException() {
      return this.oneToManyElement.getNotFound() == null || !"ignore".equals(this.oneToManyElement.getNotFound().value());
   }
}
