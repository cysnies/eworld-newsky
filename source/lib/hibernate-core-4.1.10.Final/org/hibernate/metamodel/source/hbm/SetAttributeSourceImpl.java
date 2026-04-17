package org.hibernate.metamodel.source.hbm;

import org.hibernate.internal.jaxb.mapping.hbm.JaxbSetElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.binder.AttributeSourceContainer;
import org.hibernate.metamodel.source.binder.Orderable;
import org.hibernate.metamodel.source.binder.PluralAttributeNature;
import org.hibernate.metamodel.source.binder.Sortable;

public class SetAttributeSourceImpl extends AbstractPluralAttributeSourceImpl implements Orderable, Sortable {
   public SetAttributeSourceImpl(JaxbSetElement setElement, AttributeSourceContainer container) {
      super(setElement, container);
   }

   public JaxbSetElement getPluralAttributeElement() {
      return (JaxbSetElement)super.getPluralAttributeElement();
   }

   public PluralAttributeNature getPluralAttributeNature() {
      return PluralAttributeNature.SET;
   }

   public boolean isSorted() {
      return StringHelper.isNotEmpty(this.getComparatorName());
   }

   public String getComparatorName() {
      return this.getPluralAttributeElement().getSort();
   }

   public boolean isOrdered() {
      return StringHelper.isNotEmpty(this.getOrder());
   }

   public String getOrder() {
      return this.getPluralAttributeElement().getOrderBy();
   }
}
