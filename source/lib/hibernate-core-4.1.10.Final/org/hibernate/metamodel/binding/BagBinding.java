package org.hibernate.metamodel.binding;

import org.hibernate.metamodel.domain.PluralAttribute;

public class BagBinding extends AbstractPluralAttributeBinding {
   protected BagBinding(AttributeBindingContainer container, PluralAttribute attribute, CollectionElementNature nature) {
      super(container, attribute, nature);
   }
}
