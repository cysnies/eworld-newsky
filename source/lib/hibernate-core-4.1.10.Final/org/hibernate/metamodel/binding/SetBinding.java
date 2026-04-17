package org.hibernate.metamodel.binding;

import java.util.Comparator;
import org.hibernate.metamodel.domain.PluralAttribute;

public class SetBinding extends AbstractPluralAttributeBinding {
   private Comparator comparator;

   protected SetBinding(AttributeBindingContainer container, PluralAttribute attribute, CollectionElementNature collectionElementNature) {
      super(container, attribute, collectionElementNature);
   }

   public Comparator getComparator() {
      return this.comparator;
   }

   public void setComparator(Comparator comparator) {
      this.comparator = comparator;
   }
}
