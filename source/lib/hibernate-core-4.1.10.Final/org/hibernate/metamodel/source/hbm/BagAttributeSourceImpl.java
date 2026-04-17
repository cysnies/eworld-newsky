package org.hibernate.metamodel.source.hbm;

import org.hibernate.internal.jaxb.mapping.hbm.JaxbBagElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.binder.AttributeSourceContainer;
import org.hibernate.metamodel.source.binder.Orderable;
import org.hibernate.metamodel.source.binder.PluralAttributeNature;

public class BagAttributeSourceImpl extends AbstractPluralAttributeSourceImpl implements Orderable {
   public BagAttributeSourceImpl(JaxbBagElement bagElement, AttributeSourceContainer container) {
      super(bagElement, container);
   }

   public PluralAttributeNature getPluralAttributeNature() {
      return PluralAttributeNature.BAG;
   }

   public JaxbBagElement getPluralAttributeElement() {
      return (JaxbBagElement)super.getPluralAttributeElement();
   }

   public boolean isOrdered() {
      return StringHelper.isNotEmpty(this.getOrder());
   }

   public String getOrder() {
      return this.getPluralAttributeElement().getOrderBy();
   }
}
