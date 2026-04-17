package org.hibernate.internal.jaxb.mapping.hbm;

public abstract class IdBagPluralAttributeElementAdapter implements PluralAttributeElement {
   public JaxbOneToManyElement getOneToMany() {
      return null;
   }

   public boolean isInverse() {
      return false;
   }
}
